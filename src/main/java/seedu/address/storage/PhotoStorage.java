package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;

/** Imports and reads bounded local images while keeping every resolved path inside the application folder. */
public class PhotoStorage {
    public static final String MESSAGE_INVALID = "Photo file must be a PNG or JPEG inside the application folder.";
    public static final String MESSAGE_LIMIT = "Photo must be at most 5 MiB and 4096×4096 pixels.";
    public static final String MESSAGE_UNREADABLE = "Photo file could not be read.";
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;
    private static final Logger logger = LogsCenter.getLogger(PhotoStorage.class);

    private final Path applicationFolder;

    /** Creates storage rooted in the application folder. The folder must exist before importing images. */
    public PhotoStorage(Path applicationFolder) {
        this.applicationFolder = requireNonNull(applicationFolder).toAbsolutePath().normalize();
    }

    /** Uses the actual JAR location; development uses the working folder. */
    public static PhotoStorage forApplication() {
        try {
            Path location = Path.of(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new PhotoStorage(Files.isRegularFile(location) ? location.getParent() : Path.of(""));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not locate the application folder.", e);
        }
    }

    /**
     * Validates and copies a source to a generated name under photos/. An identical current photo is a no-op.
     * Imported assets are retained after replacement/removal so history can restore their references.
     *
     * @throws PhotoException if the source or destination is unsafe, unreadable or exceeds the limits.
     */
    public String importPhoto(String source, String currentReference) throws PhotoException {
        byte[] bytes = readValidated(source);
        Optional<byte[]> current = currentReference.isEmpty() ? Optional.empty() : readPhoto(currentReference);
        if (current.isPresent() && Arrays.equals(bytes, current.get())) {
            return currentReference;
        }
        return copyToManagedFolder(bytes, source);
    }

    /** Reads a safe image or returns empty with a warning. Unsafe stored references are never opened. */
    public Optional<byte[]> readPhoto(String reference) {
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(readValidated(reference));
        } catch (PhotoException e) {
            logger.warning("Contact photo is unavailable: " + e.getMessage());
            return Optional.empty();
        }
    }

    /** Resolves both the application root and the source, rejecting lexical and symlink escapes. */
    private Path resolveInsideFolder(String reference) throws PhotoException, IOException {
        Path relative;
        try {
            relative = Path.of(reference);
        } catch (InvalidPathException e) {
            throw new PhotoException(MESSAGE_INVALID, e);
        }
        if (reference.isBlank() || relative.isAbsolute() || reference.contains("\\") || reference.contains(":")) {
            throw new PhotoException(MESSAGE_INVALID);
        }
        for (Path part : relative) {
            if (part.toString().equals("..")) {
                throw new PhotoException(MESSAGE_INVALID);
            }
        }
        Path root = applicationFolder.toRealPath();
        Path resolved = root.resolve(relative).toRealPath();
        if (!resolved.startsWith(root)) {
            throw new PhotoException(MESSAGE_INVALID);
        }
        return resolved;
    }

    /** Reads no more than the size limit plus one byte and verifies real image content before returning it. */
    private byte[] readValidated(String reference) throws PhotoException {
        String extension = extension(reference);
        try {
            Path source = resolveInsideFolder(reference);
            if (!Files.isRegularFile(source) || !Files.isReadable(source)) {
                throw new PhotoException(MESSAGE_UNREADABLE);
            }
            if (Files.size(source) > MAX_BYTES) {
                throw new PhotoException(MESSAGE_LIMIT);
            }
            byte[] bytes;
            try (InputStream input = Files.newInputStream(source)) {
                bytes = input.readNBytes(MAX_BYTES + 1);
            }
            if (bytes.length > MAX_BYTES) {
                throw new PhotoException(MESSAGE_LIMIT);
            }
            validateImage(bytes, extension);
            return bytes;
        } catch (IOException | SecurityException e) {
            throw new PhotoException(MESSAGE_UNREADABLE, e);
        }
    }

    /** Checks the extension without accepting URLs or other image formats supported by ImageIO. */
    private String extension(String reference) throws PhotoException {
        String lower = reference.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "jpeg";
        }
        throw new PhotoException(MESSAGE_INVALID);
    }

    /** Checks metadata before decoding to avoid allocating an oversized image. */
    private void validateImage(byte[] bytes, String extension) throws PhotoException {
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new PhotoException(MESSAGE_INVALID);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if (!reader.getFormatName().equalsIgnoreCase(extension)) {
                    throw new PhotoException(MESSAGE_INVALID);
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
                    throw new PhotoException(MESSAGE_LIMIT);
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null || width < 1 || height < 1) {
                    throw new PhotoException(MESSAGE_INVALID);
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new PhotoException(MESSAGE_INVALID, e);
        }
    }

    /** Creates a collision-safe destination only after confirming the managed directory stays inside the root. */
    private String copyToManagedFolder(byte[] bytes, String source) throws PhotoException {
        Path destination = null;
        try {
            Path root = applicationFolder.toRealPath();
            Path folder = root.resolve("photos");
            Files.createDirectories(folder);
            Path realFolder = folder.toRealPath();
            if (!realFolder.startsWith(root)) {
                throw new PhotoException(MESSAGE_INVALID);
            }
            destination = Files.createTempFile(realFolder, "contact-", "." + extension(source));
            Files.write(destination, bytes);
            return "photos/" + destination.getFileName();
        } catch (IOException | SecurityException e) {
            if (destination != null) {
                try {
                    Files.deleteIfExists(destination);
                } catch (IOException cleanupError) {
                    logger.warning("Could not remove an incomplete imported photo.");
                }
            }
            throw new PhotoException(MESSAGE_UNREADABLE, e);
        }
    }

    /** A user-facing validation or file-access failure, distinct from programming errors. */
    public static class PhotoException extends Exception {
        public PhotoException(String message) {
            super(message);
        }

        public PhotoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
