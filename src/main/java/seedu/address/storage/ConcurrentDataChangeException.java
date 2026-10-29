package seedu.address.storage;

import java.io.IOException;

/** Signals that another copy changed the address book after this copy loaded it. */
public class ConcurrentDataChangeException extends IOException {
    public ConcurrentDataChangeException() {
        super("Address book data changed outside this copy.");
    }
}
