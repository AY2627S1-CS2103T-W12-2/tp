package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("a".repeat(51))); // more than 50 characters
        assertFalse(Name.isValidName("-Mary")); // starts with a hyphen
        assertFalse(Name.isValidName("'Brien")); // starts with an apostrophe
        assertFalse(Name.isValidName("’Brien")); // starts with a curly apostrophe
        assertFalse(Name.isValidName("́Jose")); // starts with a combining accent
        assertFalse(Name.isValidName("Amy 😀")); // emoji
        assertFalse(Name.isValidName("Mary Jane")); // no-break space
        assertFalse(Name.isValidName("李".repeat(51))); // 51 characters in another script
        assertFalse(Name.isValidName("𠮷".repeat(51))); // 51 letters outside the Basic Multilingual Plane
        assertFalse(Name.isValidName(" -")); // starts with a space
        assertFalse(Name.isValidName("-")); // only a hyphen
        assertFalse(Name.isValidName("Mary_Jane")); // underscore
        assertFalse(Name.isValidName("Mary.Jane")); // full stop
        assertFalse(Name.isValidName("John s/o Raj")); // slash
        assertFalse(Name.isValidName("Mary\"Jane")); // double quote
        assertFalse(Name.isValidName("Mary-Jane" + "a".repeat(42))); // 51 characters with a hyphen

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("a".repeat(50))); // exactly 50 characters
        assertTrue(Name.isValidName("José")); // accented letter
        assertTrue(Name.isValidName("José")); // accent typed as a separate combining character
        assertTrue(Name.isValidName("Müller-Zoë")); // several accents and a hyphen
        assertTrue(Name.isValidName("李明")); // Chinese
        assertTrue(Name.isValidName("احمد علي")); // Arabic with a space
        assertTrue(Name.isValidName("สมชาย")); // Thai, which uses combining marks
        assertTrue(Name.isValidName("O’Brien")); // curly apostrophe, as pasted from phones and chat apps
        assertTrue(Name.isValidName("李".repeat(50))); // exactly 50 characters in another script
        assertTrue(Name.isValidName("𠮷".repeat(50))); // 50 letters outside the Basic Multilingual Plane
        assertTrue(Name.isValidName("Mary-Jane")); // hyphen
        assertTrue(Name.isValidName("Nur-Aisyah binte Ahmad")); // hyphen and spaces
        assertTrue(Name.isValidName("O'Brien")); // apostrophe
        assertTrue(Name.isValidName("D'Souza-Lim")); // apostrophe and hyphen
        assertTrue(Name.isValidName("Mary-")); // hyphen at the end
        assertTrue(Name.isValidName("1st-Class")); // starts with a digit
        assertTrue(Name.isValidName("Mary-Jane" + "a".repeat(41))); // exactly 50 characters with a hyphen
    }

    @Test
    public void constructor_separateAccent_isStoredComposed() {
        assertEquals("José", new Name("José").fullName);
        assertEquals(new Name("José"), new Name("José"));
    }

    @Test
    public void isValidName_lengthIsCountedAfterAccentsAreComposed() {
        // 50 accented letters typed as 100 characters are still 50 characters
        assertTrue(Name.isValidName("é".repeat(50)));
        assertFalse(Name.isValidName("é".repeat(51)));
    }

    @Test
    public void constraintsMessage_statesTheAllowedCharactersAndTheFirstCharacterRule() {
        assertTrue(Name.MESSAGE_CONSTRAINTS.contains("1-50 characters"));
        assertTrue(Name.MESSAGE_CONSTRAINTS.contains("letters, digits, spaces, - and '"));
        assertTrue(Name.MESSAGE_CONSTRAINTS.contains("Start with a letter or digit"));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
