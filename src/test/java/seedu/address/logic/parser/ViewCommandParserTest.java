package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INDEX_TOO_LARGE;
import static seedu.address.logic.parser.ViewCommandParser.MESSAGE_MULTIPLE_INDEXES;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

/**
 * Tests the command-specific error messages as well as valid index parsing.
 */
public class ViewCommandParserTest {

    private ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonPositiveIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "0", expectedMessage);
        assertParseFailure(parser, "-1", expectedMessage);
    }

    @Test
    public void parse_multipleIndexes_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                MESSAGE_MULTIPLE_INDEXES + "\n" + ViewCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 2", expectedMessage);
        assertParseFailure(parser, " 1   2 ", expectedMessage);
    }

    @Test
    public void parse_indexTooLarge_throwsParseException() {
        assertParseFailure(parser, Long.toString((long) Integer.MAX_VALUE + 1), MESSAGE_INDEX_TOO_LARGE);
    }
}
