package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StarCommand;

/*
 * Equivalence partitions for the argument: one positive integer (valid); empty or several words (wrong format);
 * zero, negative or not a number (invalid index). Boundary value: 0 and 1.
 */
public class StarCommandParserTest {

    private final StarCommandParser starParser = new StarCommandParser(true);
    private final StarCommandParser unstarParser = new StarCommandParser(false);

    @Test
    public void parse_validIndex_returnsStarCommand() {
        assertParseSuccess(starParser, " 1 ", new StarCommand(INDEX_FIRST_PERSON, true));
        assertParseSuccess(unstarParser, "1", new StarCommand(INDEX_FIRST_PERSON, false));
    }

    @Test
    public void parse_missingOrExtraArguments_throwsFormatErrorWithMatchingUsage() {
        assertParseFailure(starParser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, StarCommand.MESSAGE_USAGE));
        assertParseFailure(starParser, "1 2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StarCommand.MESSAGE_USAGE));
        assertParseFailure(unstarParser, "",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StarCommand.MESSAGE_UNSTAR_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsInvalidPersonIndex() {
        assertParseFailure(starParser, "0", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(starParser, "-1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(unstarParser, "abc", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
