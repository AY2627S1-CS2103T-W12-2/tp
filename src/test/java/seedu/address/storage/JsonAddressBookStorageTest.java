package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullContactEntry_reportsDataErrorAndPreservesFile() throws Exception {
        assertInvalidJsonPreserved("{\"persons\":[null]}");
    }

    @Test
    public void readAddressBook_nullTagEntry_reportsDataErrorAndPreservesFile() throws Exception {
        assertInvalidJsonPreserved("{\"persons\":[{\"name\":\"Alice\",\"tags\":[null]}]}");
    }

    @Test
    public void readAddressBook_nullRoot_reportsDataErrorAndPreservesFile() throws Exception {
        assertInvalidJsonPreserved("null");
    }

    private void assertInvalidJsonPreserved(String json) throws Exception {
        Path file = testFolder.resolve("null-elements.json");
        Files.writeString(file, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(json, Files.readString(file));
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void saveAddressBook_twoCopiesLoadedMissingFile_secondCannotOverwrite() throws Exception {
        Path file = testFolder.resolve("new-addressBook.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        assertTrue(first.readAddressBook().isEmpty());
        assertTrue(second.readAddressBook().isEmpty());
        AddressBook firstBook = new AddressBook();
        firstBook.addPerson(ALICE);
        AddressBook secondBook = new AddressBook();
        secondBook.addPerson(HOON);

        first.saveAddressBook(firstBook);

        assertThrows(ConcurrentDataChangeException.class, () -> second.saveAddressBook(secondBook));
        assertEquals(firstBook, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_repeatedIdenticalSave_keepsFileUntouched() throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook book = getTypicalAddressBook();
        storage.saveAddressBook(book);
        Files.setLastModifiedTime(file, FileTime.fromMillis(1600000000000L));
        FileTime modified = Files.getLastModifiedTime(file);
        byte[] contents = Files.readAllBytes(file);

        for (int attempt = 0; attempt < 3; attempt++) {
            storage.saveAddressBook(book);
            assertEquals(modified, Files.getLastModifiedTime(file));
            assertArrayEquals(contents, Files.readAllBytes(file));
        }
        try (var entries = Files.list(testFolder)) {
            assertEquals(Set.of(file, testFolder.resolve("addressBook.json.lock")),
                    entries.collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test
    public void saveAddressBook_twoCopiesSaveIdenticalContents_retriesAndNextChangeSucceed() throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        first.readAddressBook();
        second.readAddressBook();
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        first.saveAddressBook(book);
        Files.setLastModifiedTime(file, FileTime.fromMillis(1600000000000L));
        FileTime modified = Files.getLastModifiedTime(file);

        second.saveAddressBook(book);
        second.saveAddressBook(book);
        assertEquals(modified, Files.getLastModifiedTime(file));
        assertEquals(book, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());

        book.addPerson(HOON);
        second.saveAddressBook(book);
        assertEquals(book, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_repeatedStaleDifferentSave_preservesOtherCopyChanges() throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        first.saveAddressBook(book);
        second.readAddressBook();
        AddressBook updated = new AddressBook(book);
        updated.addPerson(HOON);
        first.saveAddressBook(updated);
        byte[] contents = Files.readAllBytes(file);

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThrows(ConcurrentDataChangeException.class, () -> second.saveAddressBook(book));
            assertArrayEquals(contents, Files.readAllBytes(file));
        }
        assertEquals(updated, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_staleReadOnlyTarget_reportsConflictBeforePermissions() throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        first.saveAddressBook(original);
        second.readAddressBook();
        AddressBook updated = new AddressBook(original);
        updated.addPerson(HOON);
        first.saveAddressBook(updated);
        byte[] contents = Files.readAllBytes(file);
        PosixFileAttributeView posix = Files.getFileAttributeView(file, PosixFileAttributeView.class);
        DosFileAttributeView dos = Files.getFileAttributeView(file, DosFileAttributeView.class);
        assumeTrue(posix != null || dos != null);
        Set<PosixFilePermission> permissions = posix == null ? null : posix.readAttributes().permissions();
        try {
            if (posix != null) {
                posix.setPermissions(Set.of(PosixFilePermission.OWNER_READ));
            } else {
                dos.setReadOnly(true);
            }
            assertThrows(ConcurrentDataChangeException.class, () -> second.saveAddressBook(original));
            assertArrayEquals(contents, Files.readAllBytes(file));
        } finally {
            if (posix != null) {
                posix.setPermissions(permissions);
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    @Test
    public void saveAddressBook_identicalSaveInReadOnlyFolder_needsNoTemporaryFile() throws Exception {
        assertSaveInReadOnlyFolder(false);
    }

    @Test
    public void saveAddressBook_staleSaveInReadOnlyFolder_reportsConflictWithoutTemporaryFile() throws Exception {
        assertSaveInReadOnlyFolder(true);
    }

    private void assertSaveInReadOnlyFolder(boolean stale) throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        first.saveAddressBook(original);
        second.readAddressBook();
        if (stale) {
            AddressBook updated = new AddressBook(original);
            updated.addPerson(HOON);
            first.saveAddressBook(updated);
        }
        byte[] contents = Files.readAllBytes(file);
        PosixFileAttributeView posix = Files.getFileAttributeView(testFolder, PosixFileAttributeView.class);
        assumeTrue(posix != null);
        Set<PosixFilePermission> permissions = posix.readAttributes().permissions();
        try {
            posix.setPermissions(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
            assumeTrue(!Files.isWritable(testFolder));
            if (stale) {
                assertThrows(ConcurrentDataChangeException.class, () -> second.saveAddressBook(original));
            } else {
                second.saveAddressBook(original);
                second.saveAddressBook(original);
            }
            assertArrayEquals(contents, Files.readAllBytes(file));
        } finally {
            posix.setPermissions(permissions);
        }
    }

    @Test
    public void saveAddressBook_otherProcessHoldsLock_reportsBusyWithoutWriting() throws Exception {
        Path file = testFolder.resolve("addressBook.json");
        Path lockFile = testFolder.resolve("addressBook.json.lock");
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Path testClasses = Path.of(StorageLockHolder.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI());
        Process holder = new ProcessBuilder(java.toString(), "-cp", testClasses.toString(),
                StorageLockHolder.class.getName(), lockFile.toString()).redirectErrorStream(true).start();
        try {
            BufferedReader output = new BufferedReader(new InputStreamReader(holder.getInputStream(),
                    StandardCharsets.UTF_8));
            assertEquals("LOCKED", output.readLine());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            assertThrows(DataFileBusyException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertFalse(Files.exists(file));
        } finally {
            holder.getOutputStream().close();
            if (!holder.waitFor(10, TimeUnit.SECONDS)) {
                holder.destroyForcibly();
                holder.waitFor();
            }
        }
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidActivityTime_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidActivityAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidNote_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidNoteAddressBook.json"));
    }

    @Test
    public void readAddressBook_v13FileWithoutNote_loadsWithEmptyNote() throws Exception {
        ReadOnlyAddressBook addressBook = readAddressBook("v13NoActivityAddressBook.json").get();

        assertTrue(addressBook.getPersonList().get(0).getNote().isEmpty());
    }

    @Test
    public void readAndSaveAddressBook_withNote_noteSurvivesReload() throws Exception {
        Path filePath = testFolder.resolve("TempNoteAddressBook.json");
        Person withNote = new PersonBuilder(ALICE).withNote("Met at booth 4, wants a React teammate").build();
        Person withoutNote = new PersonBuilder(HOON).build();
        AddressBook original = new AddressBook();
        original.addPerson(withNote);
        original.addPerson(withoutNote);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);
        AddressBook readBack = new AddressBook(storage.readAddressBook(filePath).get());

        assertEquals(original, readBack);
        assertEquals(withNote.getNote(), readBack.getPersonList().get(0).getNote());
        assertTrue(readBack.getPersonList().get(1).getNote().isEmpty());
    }

    @Test
    public void readAddressBook_v13FileWithoutActivity_loadsWithEmptyActivity() throws Exception {
        ReadOnlyAddressBook addressBook = readAddressBook("v13NoActivityAddressBook.json").get();

        assertEquals(1, addressBook.getPersonList().size());
        assertEquals(Activity.EMPTY, addressBook.getPersonList().get(0).getActivity());
    }

    @Test
    public void readAndSaveAddressBook_withActivity_activitySurvivesReload() throws Exception {
        Path filePath = testFolder.resolve("TempActivityAddressBook.json");
        Person withAll = new PersonBuilder(ALICE).withActivity(new Activity(Instant.parse("2026-10-17T04:12:33Z"),
                Instant.parse("2026-10-18T09:00:00Z"), Instant.parse("2026-10-19T10:30:15Z"))).build();
        Person createdOnly = new PersonBuilder(HOON).withActivity(
                new Activity(Instant.parse("2026-10-17T04:12:33Z"), null, null)).build();
        AddressBook original = new AddressBook();
        original.addPerson(withAll);
        original.addPerson(createdOnly);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);
        AddressBook readBack = new AddressBook(storage.readAddressBook(filePath).get());

        assertEquals(original, readBack);
        assertEquals(withAll.getActivity(), readBack.getPersonList().get(0).getActivity());
        assertEquals(createdOnly.getActivity(), readBack.getPersonList().get(1).getActivity());
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_replacesFileWithoutChangingPriorContents() throws Exception {
        Path filePath = testFolder.resolve("addressBook.json");
        Path previousVersion = testFolder.resolve("previousVersion.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        storage.saveAddressBook(original);
        Files.createLink(previousVersion, filePath);

        AddressBook updated = new AddressBook(original);
        updated.addPerson(HOON);
        storage.saveAddressBook(updated);

        assertEquals(original, storage.readAddressBook(previousVersion).orElseThrow());
        assertEquals(updated, storage.readAddressBook(filePath).orElseThrow());
    }

    @Test
    public void saveAddressBook_failedReplacement_leavesTargetAndCleansStagedFile() throws Exception {
        Path target = testFolder.resolve("addressBook.json");
        Files.createDirectory(target);
        Path sentinel = target.resolve("sentinel.txt");
        Files.writeString(sentinel, "keep");

        assertThrows(IOException.class, () -> new JsonAddressBookStorage(target)
                .saveAddressBook(getTypicalAddressBook()));

        assertEquals("keep", Files.readString(sentinel));
        try (var entries = Files.list(testFolder)) {
            assertEquals(Set.of(target, testFolder.resolve("addressBook.json.lock")),
                    entries.collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test
    public void saveAddressBook_readOnlyTarget_preservesFileAndReportsAccessDenied() throws Exception {
        Path target = testFolder.resolve("addressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(target);
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        storage.saveAddressBook(original);
        byte[] before = Files.readAllBytes(target);
        PosixFileAttributeView posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        DosFileAttributeView dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
        assumeTrue(posix != null || dos != null);

        Set<PosixFilePermission> originalPermissions = posix == null ? null : posix.readAttributes().permissions();
        try {
            if (posix != null) {
                posix.setPermissions(Set.of(PosixFilePermission.OWNER_READ));
            } else {
                dos.setReadOnly(true);
            }

            AddressBook updated = new AddressBook(original);
            updated.addPerson(HOON);
            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(updated));
            assertEquals(original, storage.readAddressBook().orElseThrow());
            assertArrayEquals(before, Files.readAllBytes(target));
        } finally {
            if (posix != null) {
                posix.setPermissions(originalPermissions);
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    @Test
    public void saveAddressBook_existingPosixPermissions_surviveReplacement() throws Exception {
        Path target = testFolder.resolve("addressBook.json");
        PosixFileAttributeView posix = Files.getFileAttributeView(testFolder, PosixFileAttributeView.class);
        assumeTrue(posix != null);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(target);
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        storage.saveAddressBook(original);
        Set<PosixFilePermission> permissions = Set.of(PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ,
                PosixFilePermission.OTHERS_READ);
        Files.setPosixFilePermissions(target, permissions);

        AddressBook updated = new AddressBook(original);
        updated.addPerson(HOON);
        storage.saveAddressBook(updated);

        assertEquals(permissions, Files.getPosixFilePermissions(target));
        assertEquals(updated, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
