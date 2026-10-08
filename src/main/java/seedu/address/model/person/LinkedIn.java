package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's LinkedIn handle in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidLinkedIn(String)}
 */
public class LinkedIn {

    public static final String MESSAGE_CONSTRAINTS = "LinkedIn handles should only contain alphanumeric characters "
            + "and hyphens, and be between 3 and 50 characters long.";

    public static final String VALIDATION_REGEX = "[A-Za-z0-9-]{3,50}";

    public final String value;

    /**
     * Constructs a {@code LinkedIn}.
     *
     * @param linkedIn A valid LinkedIn handle, or an empty string if the person has no handle.
     */
    public LinkedIn(String linkedIn) {
        requireNonNull(linkedIn);
        checkArgument(isValidLinkedIn(linkedIn), MESSAGE_CONSTRAINTS);
        value = linkedIn;
    }

    /**
     * Returns true if a given string is a valid LinkedIn handle.
     * An empty string is valid and represents the absence of a handle.
     */
    public static boolean isValidLinkedIn(String test) {
        return test.isEmpty() || test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if the person has no LinkedIn handle.
     */
    public boolean isEmpty() {
        return value.isEmpty();
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
        if (!(other instanceof LinkedIn otherLinkedIn)) {
            return false;
        }

        return value.equals(otherLinkedIn.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
