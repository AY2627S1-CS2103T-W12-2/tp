package seedu.address.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Holds a data-file lock in a separate JVM for the storage concurrency test.
 */
public final class StorageLockHolder {
    private StorageLockHolder() {}

    /**
     * Acquires the lock passed as the first argument until standard input closes.
     */
    public static void main(String[] args) throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of(args[0]),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            System.out.println("LOCKED");
            System.in.read();
        }
    }
}
