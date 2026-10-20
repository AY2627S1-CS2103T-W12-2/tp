package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.EventContainsTermsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }


    @Test
    public void parse_eventTerms_returnsEventFindCommand() {
        var expected = new FindCommand(new EventContainsTermsPredicate(
                List.of("Hack Singapore", "AI")));
        assertParseSuccess(parser, " ev/Hack Singapore ev/AI ", expected);
    }

    @Test
    public void parse_slashContainingEvent_preservesEventText() {
        for (String event : new String[]{"Conference c/o NUS", "ai/ml workshop", "Conference https://nus.edu.sg"}) {
            assertParseSuccess(parser, " ev/" + event,
                    new FindCommand(new EventContainsTermsPredicate(List.of(event))));
        }
    }

    @Test
    public void parse_emptyEventOrMixedSearch_rejectsInvalidFormat() {
        for (String args : new String[]{" ev/", " ev/Hack ev/", " ev/Hack r/Mentor", " Alice ev/Hack"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

}
