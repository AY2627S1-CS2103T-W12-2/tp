package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's free-form note in the address book.
 * The note may contain spaces and punctuation. An empty note means that the person has no note.
 * Guarantees: immutable; is valid as declared in {@link #isValidNote(String)}; surrounding whitespace is removed.
 */
public class Note {

    public static final int MAX_LENGTH = 1000;

    public static final String MESSAGE_BLANK = "Note cannot be blank. Use 'note INDEX clear' to remove it.";
    public static final String MESSAGE_TOO_LONG = "Note must be at most " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_CONSTRAINTS =
            "Notes must not be blank and must be at most " + MAX_LENGTH + " characters.";

    /** The note of a person who has no note. */
    public static final Note EMPTY = new Note("");

    public final String value;

    /**
     * Constructs a {@code Note}. Surrounding whitespace is removed.
     *
     * @param note A valid note, or an empty string if the person has no note.
     */
    public Note(String note) {
        requireNonNull(note);
        checkArgument(isValidNote(note), MESSAGE_CONSTRAINTS);
        value = note.strip();
    }

    /**
     * Returns true if a given string is a valid note: either empty (no note), or not blank and at most
     * {@value #MAX_LENGTH} characters long once surrounding whitespace is removed.
     */
    public static boolean isValidNote(String test) {
        requireNonNull(test);
        return test.isEmpty() || (!test.isBlank() && test.strip().length() <= MAX_LENGTH);
    }

    /**
     * Returns true if the person has no note.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Returns a description of this note that is safe to write to the log: only how long the note is,
     * never its text, because a note is personal.
     */
    public String toLogString() {
        return isEmpty() ? "none" : "[" + value.length() + " characters]";
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Note otherNote)) {
            return false;
        }

        return value.equals(otherNote.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
