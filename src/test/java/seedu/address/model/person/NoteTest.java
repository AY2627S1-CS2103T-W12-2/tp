package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalNotes.EMOJI;

import org.junit.jupiter.api.Test;

public class NoteTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Note(null));
    }

    @Test
    public void constructor_invalidNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Note("   "));
        assertThrows(IllegalArgumentException.class, () -> new Note("a".repeat(Note.MAX_LENGTH + 1)));
    }

    @Test
    public void constructor_validNote_removesSurroundingWhitespaceOnly() {
        String text = "Met at the AI track; wants a React teammate (Sat 10am) - \"great\" ideas, a@b.com! #hack";

        assertEquals(text, new Note("  " + text + " \t").value);
    }

    /*
     * Equivalence partitions for the text of a note (length is measured after surrounding whitespace is removed):
     *   - empty (no note)                  - valid
     *   - blank (only whitespace)          - invalid
     *   - 1 to MAX_LENGTH characters       - valid
     *   - more than MAX_LENGTH characters  - invalid
     * Boundary values: 0, 1, MAX_LENGTH and MAX_LENGTH + 1 characters, and the same lengths with padding.
     */
    @Test
    public void isValidNote() {
        assertThrows(NullPointerException.class, () -> Note.isValidNote(null));

        // empty and blank
        assertTrue(Note.isValidNote(""));
        assertFalse(Note.isValidNote("   \t  "));

        // lower boundary of the valid length range
        assertTrue(Note.isValidNote("a"));

        // upper boundary, with and without padding that must not count towards the length
        assertTrue(Note.isValidNote("a".repeat(Note.MAX_LENGTH)));
        assertTrue(Note.isValidNote("  " + "a".repeat(Note.MAX_LENGTH) + "  "));
        assertFalse(Note.isValidNote("a".repeat(Note.MAX_LENGTH + 1)));
    }

    /*
     * The limit counts characters, not Java chars: an emoji such as U+1F600 is one character, although Java stores
     * it as two chars (a surrogate pair). Boundary values: MAX_LENGTH and MAX_LENGTH + 1 characters made of emoji,
     * and of emoji mixed with letters, which are one char each.
     */
    @Test
    public void isValidNote_emoji_countsEachEmojiAsOneCharacter() {
        assertTrue(Note.isValidNote(EMOJI.repeat(Note.MAX_LENGTH / 2 + 1)));
        assertTrue(Note.isValidNote(EMOJI.repeat(Note.MAX_LENGTH)));
        assertTrue(Note.isValidNote("a".repeat(Note.MAX_LENGTH - 1) + EMOJI));
        assertTrue(Note.isValidNote("  " + EMOJI.repeat(Note.MAX_LENGTH) + "  "));
        assertTrue(Note.isValidNote("é".repeat(Note.MAX_LENGTH)));

        assertFalse(Note.isValidNote(EMOJI.repeat(Note.MAX_LENGTH + 1)));
        assertFalse(Note.isValidNote("a".repeat(Note.MAX_LENGTH) + EMOJI));
    }

    @Test
    public void countCharacters_countsEachCodePointOnce() {
        assertEquals(0, Note.countCharacters(""));
        assertEquals(3, Note.countCharacters("abc"));
        assertEquals(1, Note.countCharacters(EMOJI));
        assertEquals(4, Note.countCharacters("ab" + EMOJI + EMOJI));
        assertEquals(1, Note.countCharacters("é"));
        assertThrows(NullPointerException.class, () -> Note.countCharacters(null));
    }

    @Test
    public void getLengthAndLogString_emoji_useTheSameCount() {
        Note note = new Note("hi " + EMOJI.repeat(7));

        assertEquals(10, note.getLength());
        assertEquals("[10 characters]", note.toLogString());
        assertEquals("none", Note.EMPTY.toLogString());
    }

    @Test
    public void constructor_longestNoteOfEmoji_isAccepted() {
        assertEquals(EMOJI.repeat(Note.MAX_LENGTH), new Note(EMOJI.repeat(Note.MAX_LENGTH)).value);
    }

    @Test
    public void isEmpty() {
        assertTrue(Note.EMPTY.isEmpty());
        assertFalse(new Note("x").isEmpty());
    }

    @Test
    public void equals() {
        Note note = new Note("met at booth 4");

        assertTrue(note.equals(note));
        assertTrue(note.equals(new Note(" met at booth 4 ")));
        assertEquals(note.hashCode(), new Note("met at booth 4").hashCode());

        assertFalse(note.equals(null));
        assertFalse(note.equals("met at booth 4"));
        // a free-form note is compared exactly, including its case
        assertFalse(note.equals(new Note("Met at booth 4")));
        assertFalse(note.equals(Note.EMPTY));
    }

    @Test
    public void toLogString_showsOnlyTheLengthOfTheNote() {
        assertEquals("none", Note.EMPTY.toLogString());
        assertEquals("[12 characters]", new Note("private text").toLogString());
        assertFalse(new Note("private text").toLogString().contains("private"));
    }

    @Test
    public void toStringMethod() {
        assertEquals("met at booth 4", new Note("met at booth 4").toString());
    }
}
