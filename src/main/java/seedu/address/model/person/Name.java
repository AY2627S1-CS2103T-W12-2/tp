package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
        "Names must be 1-50 characters: letters, digits, spaces, - and '. "
                + "Start with a letter or digit.";

    /*
     * Letters of any language, combining marks (so that an accent typed as a separate character is accepted),
     * digits, spaces, hyphens and apostrophes (straight or curly). The first character must be a letter or a
     * digit, so a name cannot start with a space, hyphen or apostrophe, and " " (a blank string) is not valid.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{Nd}][\\p{L}\\p{M}\\p{Nd} '’-]*";

    private static final int MAX_LENGTH = 50;

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     * The length limit counts characters as a user sees them, so a letter outside the Basic Multilingual Plane
     * counts once.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX) && test.codePointCount(0, test.length()) <= MAX_LENGTH;
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
