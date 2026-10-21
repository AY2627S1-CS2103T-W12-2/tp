package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;

public class FilterCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            FilterCommand.MESSAGE_USAGE);

    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_starred_returnsFilterOn() {
        assertParseSuccess(parser, "starred", new FilterCommand(true));
        assertParseSuccess(parser, "  STARRED  ", new FilterCommand(true));
    }

    @Test
    public void parse_starredOff_returnsFilterOff() {
        assertParseSuccess(parser, "starred off", new FilterCommand(false));
        assertParseSuccess(parser, " Starred   OFF ", new FilterCommand(false));
    }

    @Test
    public void parse_anythingElse_throwsFormatError() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "off", INVALID_FORMAT);
        assertParseFailure(parser, "important", INVALID_FORMAT);
        assertParseFailure(parser, "starred on", INVALID_FORMAT);
        assertParseFailure(parser, "starred off now", INVALID_FORMAT);
    }
}
