package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalNotes.EMOJI;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.Note;

/**
 * Tests {@code NoteCommandParser} with one representative input for each equivalence partition, and the
 * boundary values of the note length. For example, {@code "-1 nt/x"} is not tested because it is in the same
 * partition as {@code "0 nt/x"} (an index that is not positive).
 */
public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    private static String format(String hint) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, hint);
    }

    @Test
    public void parse_setNote_returnsNoteCommand() {
        assertParseSuccess(parser, " 2 nt/Met at booth 4", new NoteCommand(INDEX_SECOND_PERSON,
                new Note("Met at booth 4")));
    }

    @Test
    public void parse_noteWithPunctuationAndSlashes_isKeptAsTyped() {
        // a prefix-like text inside a word, such as ant/, does not start a new field
        String text = "Met at the AI track; (Sat 10am) - \"great\", a@b.com! #hack, see agent/notes and ant/bee";

        assertParseSuccess(parser, "1 nt/" + text, new NoteCommand(INDEX_FIRST_PERSON, new Note(text)));
    }

    @Test
    public void parse_surroundingWhitespace_isTrimmed() {
        assertParseSuccess(parser, "   1   nt/   spaced out   ",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("spaced out")));
    }

    @Test
    public void parse_noteOfEmoji_countsEachEmojiAsOneCharacter() {
        String longest = EMOJI.repeat(Note.MAX_LENGTH);

        assertParseSuccess(parser, "1 nt/" + longest, new NoteCommand(INDEX_FIRST_PERSON, new Note(longest)));
        assertParseSuccess(parser, "1 nt/" + EMOJI.repeat(Note.MAX_LENGTH / 2 + 1), new NoteCommand(
                INDEX_FIRST_PERSON, new Note(EMOJI.repeat(Note.MAX_LENGTH / 2 + 1))));
        assertParseFailure(parser, "1 nt/" + longest + EMOJI, Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_noteLengthBoundaries() {
        String longest = "a".repeat(Note.MAX_LENGTH);

        // longest accepted note, also when padded
        assertParseSuccess(parser, "1 nt/" + longest, new NoteCommand(INDEX_FIRST_PERSON, new Note(longest)));
        assertParseSuccess(parser, "1 nt/  " + longest + "  ", new NoteCommand(INDEX_FIRST_PERSON,
                new Note(longest)));
        // one character too many
        assertParseFailure(parser, "1 nt/" + longest + "a", Note.MESSAGE_TOO_LONG);
        // shortest accepted note, and the empty and blank notes just below it
        assertParseSuccess(parser, "1 nt/a", new NoteCommand(INDEX_FIRST_PERSON, new Note("a")));
        assertParseFailure(parser, "1 nt/", Note.MESSAGE_BLANK);
        assertParseFailure(parser, "1 nt/     ", Note.MESSAGE_BLANK);
    }

    @Test
    public void parse_clear_returnsNoteCommandWithEmptyNote() {
        assertParseSuccess(parser, "  2   CLEAR  ", new NoteCommand(INDEX_SECOND_PERSON, Note.EMPTY));
    }

    @Test
    public void parse_invalidIndex_throwsDisplayedIndexMessage() {
        // not positive (boundary 0), not a number, too large for an int
        assertParseFailure(parser, "0 nt/text", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "abc nt/text", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "99999999999 clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_wrongShape_throwsHintThatFitsTheMistake() {
        // no index: nothing at all, or only a note
        assertParseFailure(parser, "", format(NoteCommand.MESSAGE_MISSING_INDEX));
        // the arguments after the command word start with a space, as they do when typed in the app
        assertParseFailure(parser, " nt/text", format(NoteCommand.MESSAGE_MISSING_INDEX));
        // a lone clear has the operation but not the index, in any letter case
        assertParseFailure(parser, "clear", format(NoteCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " CLEAR ", format(NoteCommand.MESSAGE_MISSING_INDEX));
        // an index on its own
        assertParseFailure(parser, "1", format(NoteCommand.MESSAGE_MISSING_NOTE));
        // text without the prefix, or one word that is not clear
        assertParseFailure(parser, "1 some text", format(NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX));
        assertParseFailure(parser, "1 foo", format(NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX));
        // both forms together
        assertParseFailure(parser, "1 clear nt/text", format(NoteCommand.MESSAGE_NOT_BOTH));
        // too many parameters: before the prefix, or after clear
        assertParseFailure(parser, "1 2 nt/text", format(NoteCommand.MESSAGE_TOO_MANY_PARAMETERS));
        assertParseFailure(parser, "1 clear extra", format(NoteCommand.MESSAGE_TOO_MANY_PARAMETERS));
        // the shape is checked before the index, so a bad index in a wrong shape still gives the format error
        assertParseFailure(parser, "abc", format(NoteCommand.MESSAGE_MISSING_NOTE));
        assertParseFailure(parser, "abc some text", format(NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX));
    }

    /**
     * Pins the wording of the hints and keeps them short. The 70-character budget only guards against verbose
     * hints; it does not show how the text is laid out. {@code ResultDisplayTest} checks that the complete
     * messages fit the result box without scrolling at the minimum window width.
     */
    @Test
    public void hints_haveTheAgreedWordingAndStayWithinACharacterBudget() {
        assertEquals("Add the contact's INDEX: note INDEX nt/NOTE or note INDEX clear.",
                NoteCommand.MESSAGE_MISSING_INDEX);
        assertEquals("Add nt/NOTE, or use: note INDEX clear.", NoteCommand.MESSAGE_MISSING_NOTE);
        assertEquals("Put the note after nt/: note INDEX nt/NOTE.", NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX);
        assertEquals("Too many parameters. Use: note INDEX nt/NOTE or note INDEX clear.",
                NoteCommand.MESSAGE_TOO_MANY_PARAMETERS);
        assertEquals("Use nt/NOTE or clear, not both.", NoteCommand.MESSAGE_NOT_BOTH);
        for (String hint : new String[] {NoteCommand.MESSAGE_USAGE, NoteCommand.MESSAGE_MISSING_INDEX,
            NoteCommand.MESSAGE_MISSING_NOTE, NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX,
            NoteCommand.MESSAGE_TOO_MANY_PARAMETERS, NoteCommand.MESSAGE_NOT_BOTH}) {
            assertTrue(hint.length() <= 70, hint);
        }
    }

    @Test
    public void parse_repeatedPrefix_throwsDuplicateFieldsMessage() {
        assertParseFailure(parser, "1 nt/one nt/two",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE));
    }
}
