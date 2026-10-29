package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static String format(String hint) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, hint);
    }

    @Test
    public void parse_missingIndex_throwsHintNamingTheCommand() {
        assertParseFailure(starParser, "", format(StarCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(starParser, "   ", format(StarCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(unstarParser, "", format(StarCommand.MESSAGE_UNSTAR_MISSING_INDEX));
    }

    @Test
    public void parse_extraWords_throwsHintNamingTheCommand() {
        assertParseFailure(starParser, "1 2", format(StarCommand.MESSAGE_EXTRA_WORDS));
        assertParseFailure(starParser, "1 junk", format(StarCommand.MESSAGE_EXTRA_WORDS));
        assertParseFailure(unstarParser, "1 2", format(StarCommand.MESSAGE_UNSTAR_EXTRA_WORDS));
    }

    @Test
    public void hints_haveTheAgreedWording() {
        assertEquals("Add the contact's INDEX: star INDEX.", StarCommand.MESSAGE_MISSING_INDEX);
        assertEquals("Add the contact's INDEX: unstar INDEX.", StarCommand.MESSAGE_UNSTAR_MISSING_INDEX);
        assertEquals("Use one INDEX only: star INDEX.", StarCommand.MESSAGE_EXTRA_WORDS);
        assertEquals("Use one INDEX only: unstar INDEX.", StarCommand.MESSAGE_UNSTAR_EXTRA_WORDS);
    }

    @Test
    public void parse_invalidIndex_throwsInvalidPersonIndex() {
        assertParseFailure(starParser, "0", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(starParser, "-1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(unstarParser, "abc", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
