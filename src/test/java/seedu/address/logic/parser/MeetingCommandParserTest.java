package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MeetingCommand;

public class MeetingCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            MeetingCommand.MESSAGE_USAGE);

    private final MeetingCommandParser parser = new MeetingCommandParser();

    /*
     * The word clear comes after the index. A command that starts with it has no index, or has the words in the
     * wrong order, so it reports the usage and not an invalid index.
     */
    @Test
    public void parse_clearInPlaceOfTheIndex_throwsUsageAndNotInvalidIndex() {
        assertParseFailure(parser, " clear", INVALID_FORMAT);
        assertParseFailure(parser, " CLEAR", INVALID_FORMAT);
        assertParseFailure(parser, " clear 1", INVALID_FORMAT);
        assertParseFailure(parser, " clear date", INVALID_FORMAT);
    }

    @Test
    public void parse_wrongIndexBeforeClear_stillThrowsInvalidIndex() {
        assertParseFailure(parser, " 0 clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, " abc clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_indexThenClear_stillParses() throws Exception {
        // MeetingCommand has no equals, so check the kind of command that comes back
        assertTrue(parser.parse(" 1 clear") instanceof MeetingCommand);
        assertTrue(parser.parse(" 1 clear date") instanceof MeetingCommand);
        // clear is accepted in any letter case in the second position too, as it is in the first
        assertTrue(parser.parse(" 1 CLEAR") instanceof MeetingCommand);
        assertTrue(parser.parse(" 1 Clear date") instanceof MeetingCommand);
    }
}
