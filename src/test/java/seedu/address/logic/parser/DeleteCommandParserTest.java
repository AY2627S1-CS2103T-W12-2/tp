package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DeleteCommandParser.MESSAGE_MULTIPLE_INDEXES;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;

/**
 * Tests the command-specific error messages as well as valid index parsing.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, Integer.toString(Integer.MAX_VALUE),
                new DeleteCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingIndex_throwsInvalidCommandFormat() {
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidArgs_throwsInvalidPersonIndex() {
        assertParseFailure(parser, "abc", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_nonPositiveIndex_throwsInvalidPersonIndex() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_multipleIndexes_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                MESSAGE_MULTIPLE_INDEXES + "\n" + DeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 2", expectedMessage);
        assertParseFailure(parser, " 1   2 ", expectedMessage);
    }

    @Test
    public void parse_indexTooLarge_throwsInvalidPersonIndex() {
        assertParseFailure(parser, Long.toString((long) Integer.MAX_VALUE + 1),
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
