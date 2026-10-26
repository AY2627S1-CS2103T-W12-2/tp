package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

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

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            NoteCommand.MESSAGE_USAGE);

    private final NoteCommandParser parser = new NoteCommandParser();

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
        // U+1F600 is one character, although Java stores it as two chars
        String emoji = new String(Character.toChars(0x1F600));
        String longest = emoji.repeat(Note.MAX_LENGTH);

        assertParseSuccess(parser, "1 nt/" + longest, new NoteCommand(INDEX_FIRST_PERSON, new Note(longest)));
        assertParseSuccess(parser, "1 nt/" + emoji.repeat(Note.MAX_LENGTH / 2 + 1), new NoteCommand(
                INDEX_FIRST_PERSON, new Note(emoji.repeat(Note.MAX_LENGTH / 2 + 1))));
        assertParseFailure(parser, "1 nt/" + longest + emoji, Note.MESSAGE_TOO_LONG);
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
    public void parse_wrongShape_throwsInvalidCommandFormat() {
        // nothing at all, and an index on its own
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "1", INVALID_FORMAT);
        // no index
        assertParseFailure(parser, "nt/text", INVALID_FORMAT);
        // text without the prefix, or one word that is not clear
        assertParseFailure(parser, "1 some text", INVALID_FORMAT);
        assertParseFailure(parser, "1 foo", INVALID_FORMAT);
        // both forms together
        assertParseFailure(parser, "1 clear nt/text", INVALID_FORMAT);
        // extra words before the prefix
        assertParseFailure(parser, "1 2 nt/text", INVALID_FORMAT);
        // the shape is checked before the index, so a bad index in a wrong shape still gives the format error
        assertParseFailure(parser, "abc", INVALID_FORMAT);
        assertParseFailure(parser, "abc some text", INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefix_throwsDuplicateFieldsMessage() {
        assertParseFailure(parser, "1 nt/one nt/two",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE));
    }
}
