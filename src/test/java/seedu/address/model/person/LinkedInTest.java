package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LinkedInTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LinkedIn(null));
    }

    @Test
    public void constructor_invalidLinkedIn_throwsIllegalArgumentException() {
        String invalidLinkedIn = "ab";
        assertThrows(IllegalArgumentException.class, () -> new LinkedIn(invalidLinkedIn));
    }

    @Test
    public void isValidLinkedIn() {
        // null LinkedIn handle
        assertThrows(NullPointerException.class, () -> LinkedIn.isValidLinkedIn(null));

        // invalid LinkedIn handles
        assertFalse(LinkedIn.isValidLinkedIn(" ")); // spaces only
        assertFalse(LinkedIn.isValidLinkedIn("ab")); // less than 3 characters
        assertFalse(LinkedIn.isValidLinkedIn("a".repeat(51))); // more than 50 characters
        assertFalse(LinkedIn.isValidLinkedIn("john doe")); // space within handle
        assertFalse(LinkedIn.isValidLinkedIn("john_doe")); // underscore
        assertFalse(LinkedIn.isValidLinkedIn("john@doe")); // symbol
        assertFalse(LinkedIn.isValidLinkedIn("linkedin.com/in/john-doe")); // full URL

        // valid LinkedIn handles
        assertTrue(LinkedIn.isValidLinkedIn("")); // empty string represents no handle
        assertTrue(LinkedIn.isValidLinkedIn("abc")); // exactly 3 characters
        assertTrue(LinkedIn.isValidLinkedIn("a".repeat(50))); // exactly 50 characters
        assertTrue(LinkedIn.isValidLinkedIn("john-doe-profile")); // hyphens
        assertTrue(LinkedIn.isValidLinkedIn("JohnDoe123")); // mixed case with digits
    }

    @Test
    public void isEmpty() {
        assertTrue(new LinkedIn("").isEmpty());
        assertFalse(new LinkedIn("john-doe").isEmpty());
    }

    @Test
    public void equals() {
        LinkedIn linkedIn = new LinkedIn("john-doe");

        // same values -> returns true
        assertTrue(linkedIn.equals(new LinkedIn("john-doe")));

        // same object -> returns true
        assertTrue(linkedIn.equals(linkedIn));

        // null -> returns false
        assertFalse(linkedIn.equals(null));

        // different types -> returns false
        assertFalse(linkedIn.equals(5.0f));

        // different values -> returns false
        assertFalse(linkedIn.equals(new LinkedIn("jane-doe")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new LinkedIn("john-doe").hashCode(), new LinkedIn("john-doe").hashCode());
    }
}
