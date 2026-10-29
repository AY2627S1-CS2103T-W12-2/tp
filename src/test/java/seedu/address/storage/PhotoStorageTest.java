package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PhotoStorageTest {
    @TempDir
    private Path folder;

    @Test
    public void importPhoto_copiesWithSafeNamesAndRetainsAssets() throws Exception {
        writeImage("source.png", 12, 10);
        PhotoStorage storage = new PhotoStorage(folder);
        String first = storage.importPhoto("source.png", "");
        assertTrue(first.startsWith("photos/"));
        assertArrayEquals(Files.readAllBytes(folder.resolve("source.png")),
                Files.readAllBytes(folder.resolve(first)));
        assertEquals(first, storage.importPhoto("source.png", first));
        String second = storage.importPhoto("source.png", "");
        assertFalse(first.equals(second));
        assertTrue(Files.exists(folder.resolve(first)));
    }

    @Test
    public void importPhoto_unsafePathsAndSymlinks_rejectWithoutReadingOutside() throws Exception {
        Path outside = Files.createTempFile("outside-photo", ".png");
        try {
            Files.createSymbolicLink(folder.resolve("escape.png"), outside);
            PhotoStorage storage = new PhotoStorage(folder);
            for (String path : new String[]{outside.toString(), "../outside.png", "a/../source.png",
                "escape.png", "https://example.com/a.png", "C:\\photo.png"}) {
                var error = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto(path, ""));
                assertEquals(PhotoStorage.MESSAGE_PATH, error.getMessage());
            }
            assertTrue(storage.readPhoto("../outside.png").isEmpty());
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    public void importPhoto_managedDirectorySymlink_rejectsEscape() throws Exception {
        writeImage("source.png", 1, 1);
        Path outside = Files.createTempDirectory("outside-photos");
        try {
            Files.createSymbolicLink(folder.resolve("photos"), outside);
            PhotoStorage storage = new PhotoStorage(folder);
            var error = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("source.png", ""));
            assertEquals(PhotoStorage.MESSAGE_IMPORT, error.getMessage());
            try (var contents = Files.list(outside)) {
                assertEquals(0, contents.count());
            }
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    public void importPhoto_invalidAndMissingFiles_reportUsefulErrors() throws Exception {
        PhotoStorage storage = new PhotoStorage(folder);
        Files.writeString(folder.resolve("fake.png"), "not an image");
        writeImage("wrong.txt", 2, 2);
        for (String path : new String[]{"fake.png", "wrong.txt"}) {
            var error = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto(path, ""));
            assertEquals(PhotoStorage.MESSAGE_INVALID, error.getMessage());
        }
        var missing = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("absent.png", ""));
        assertEquals(PhotoStorage.MESSAGE_UNREADABLE, missing.getMessage());
        assertTrue(storage.readPhoto("photos/missing.png").isEmpty());
    }

    @Test
    public void importPhoto_enforcesSizeAndDimensionBoundaries() throws Exception {
        PhotoStorage storage = new PhotoStorage(folder);
        writeImage("valid.png", 4096, 1);
        storage.importPhoto("valid.png", "");
        writeImage("too-wide.png", 4097, 1);
        var dimensions = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("too-wide.png", ""));
        assertEquals(PhotoStorage.MESSAGE_LIMIT, dimensions.getMessage());
        Files.write(folder.resolve("large.png"), new byte[5 * 1024 * 1024 + 1]);
        var size = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("large.png", ""));
        assertEquals(PhotoStorage.MESSAGE_LIMIT, size.getMessage());
    }

    @Test
    public void importPhoto_jpegAndExactByteLimit_supportedButMismatchedContentRejected() throws Exception {
        PhotoStorage storage = new PhotoStorage(folder);
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB),
                "jpeg", folder.resolve("source.jpeg").toFile());
        assertTrue(storage.importPhoto("source.jpeg", "").endsWith(".jpeg"));
        Files.copy(folder.resolve("source.jpeg"), folder.resolve("wrong.png"));
        var wrongType = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("wrong.png", ""));
        assertEquals(PhotoStorage.MESSAGE_INVALID, wrongType.getMessage());
        writeImage("exact.png", 1, 1);
        byte[] exact = java.util.Arrays.copyOf(Files.readAllBytes(folder.resolve("exact.png")), 5 * 1024 * 1024);
        Files.write(folder.resolve("exact.png"), exact);
        assertTrue(storage.importPhoto("exact.png", "").startsWith("photos/"));
    }

    private void writeImage(String name, int width, int height) throws Exception {
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve(name).toFile());
    }

    @Test
    public void importPhoto_invalidPathAndDirectory_reportValidationErrors() throws Exception {
        PhotoStorage storage = new PhotoStorage(folder);
        var invalid = assertThrows(PhotoStorage.PhotoException.class, () ->
                storage.importPhoto("invalid\u0000.png", ""));
        assertEquals(PhotoStorage.MESSAGE_PATH, invalid.getMessage());
        Files.createDirectory(folder.resolve("directory.png"));
        var directory = assertThrows(PhotoStorage.PhotoException.class, () ->
                storage.importPhoto("directory.png", ""));
        assertEquals(PhotoStorage.MESSAGE_UNREADABLE, directory.getMessage());
        assertTrue(storage.readPhoto("").isEmpty());
    }

    @Test
    public void importPhoto_truncatedImage_rejectsUndecodableContent() throws Exception {
        writeImage("truncated.png", 10, 10);
        byte[] original = Files.readAllBytes(folder.resolve("truncated.png"));
        Files.write(folder.resolve("truncated.png"), java.util.Arrays.copyOf(original, 33));
        var error = assertThrows(PhotoStorage.PhotoException.class, () ->
                new PhotoStorage(folder).importPhoto("truncated.png", ""));
        assertEquals(PhotoStorage.MESSAGE_INVALID, error.getMessage());
    }

    @Test
    public void importPhoto_blockedDestination_reportsFailureAndPreservesSource() throws Exception {
        writeImage("source.png", 1, 1);
        Files.writeString(folder.resolve("photos"), "not a directory");
        var error = assertThrows(PhotoStorage.PhotoException.class, () ->
                new PhotoStorage(folder).importPhoto("source.png", ""));
        assertEquals(PhotoStorage.MESSAGE_IMPORT, error.getMessage());
        assertTrue(Files.isRegularFile(folder.resolve("source.png")));
    }

    @Test
    public void importPhoto_replacementAndMissingPreviousPhoto_importNewAssets() throws Exception {
        writeImage("source.png", 1, 1);
        writeImage("replacement.png", 2, 2);
        PhotoStorage storage = new PhotoStorage(folder);
        String first = storage.importPhoto("source.png", "");
        String replacement = storage.importPhoto("replacement.png", first);
        assertFalse(first.equals(replacement));
        assertArrayEquals(Files.readAllBytes(folder.resolve("replacement.png")),
                storage.readPhoto(replacement).orElseThrow());
        assertTrue(Files.exists(folder.resolve(first)));
        assertTrue(storage.importPhoto("source.png", "missing.png").startsWith("photos/"));
        writeImage("tall.png", 1, 4097);
        var error = assertThrows(PhotoStorage.PhotoException.class, () -> storage.importPhoto("tall.png", ""));
        assertEquals(PhotoStorage.MESSAGE_LIMIT, error.getMessage());
    }

}
