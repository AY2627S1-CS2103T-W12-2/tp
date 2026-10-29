package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final Map<Path, Optional<String>> knownVersions = new HashMap<>();

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public synchronized Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        Path target = filePath.toAbsolutePath().normalize();
        try {
            Optional<byte[]> contents = readContents(target);
            knownVersions.put(target, versionOf(contents));
            if (contents.isEmpty()) {
                return Optional.empty();
            }
            JsonSerializableAddressBook json = JsonUtil.fromJsonString(
                    new String(contents.get(), StandardCharsets.UTF_8), JsonSerializableAddressBook.class);
            if (json == null) {
                throw new IOException("JSON file must not contain only null.");
            }
            return Optional.of(json.toModelType());
        } catch (IOException e) {
            logger.warning("Error reading address book file " + target + ": " + e);
            throw new DataLoadingException(e);
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + target + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public synchronized void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path target = filePath.toAbsolutePath().normalize();
        Files.createDirectories(target.getParent());
        Path lockPath = target.resolveSibling(target.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (FileLock lock = channel.tryLock()) {
                if (lock == null) {
                    throw new DataFileBusyException();
                }
                Optional<String> currentVersion = versionOf(readContents(target));
                byte[] contents = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook))
                        .getBytes(StandardCharsets.UTF_8);
                Optional<String> savedVersion = versionOf(Optional.of(contents));
                // Identical retries and conflicts need no temporary file or data-file write permission.
                if (!savedVersion.equals(currentVersion)) {
                    if (knownVersions.containsKey(target) && !knownVersions.get(target).equals(currentVersion)) {
                        throw new ConcurrentDataChangeException();
                    }
                    saveUnderLock(contents, target);
                }
                knownVersions.put(target, savedVersion);
            } catch (OverlappingFileLockException e) {
                throw new DataFileBusyException(e);
            }
        }
    }

    /** Stages the serialized bytes and atomically replaces the data file while the companion lock is held. */
    private static void saveUnderLock(byte[] contents, Path target) throws IOException {
        Set<PosixFilePermission> permissions = null;
        if (Files.exists(target)) {
            if (!Files.isWritable(target)) {
                throw new AccessDeniedException(target.toString());
            }
            PosixFileAttributeView posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
            if (posix != null) {
                permissions = posix.readAttributes().permissions();
                if (!permissions.contains(PosixFilePermission.OWNER_WRITE)
                        && !permissions.contains(PosixFilePermission.GROUP_WRITE)
                        && !permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
                    throw new AccessDeniedException(target.toString());
                }
            }
            DosFileAttributeView dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
            if (dos != null && dos.readAttributes().isReadOnly()) {
                throw new AccessDeniedException(target.toString());
            }
        }
        Path staged = Files.createTempFile(target.getParent(), "addressBook-", ".tmp");
        try {
            Files.write(staged, contents);
            if (permissions != null) {
                Files.setPosixFilePermissions(staged, permissions);
            }
            Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    /** Reads one complete version of the file for both parsing and change detection. */
    private static Optional<byte[]> readContents(Path target) throws IOException {
        return Files.exists(target) ? Optional.of(Files.readAllBytes(target)) : Optional.empty();
    }

    /** A missing file has an empty version; present files use a content hash. */
    private static Optional<String> versionOf(Optional<byte[]> contents) {
        if (contents.isEmpty()) {
            return Optional.empty();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(contents.get());
            return Optional.of(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

}
