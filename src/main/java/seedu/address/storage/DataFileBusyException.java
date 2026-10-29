package seedu.address.storage;

import java.io.IOException;

/** Signals that another copy is writing the same address book file. */
public class DataFileBusyException extends IOException {
    public DataFileBusyException() {
        super("Address book data file is busy.");
    }

    public DataFileBusyException(Throwable cause) {
        super("Address book data file is busy.", cause);
    }
}
