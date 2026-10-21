package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TypedSearchPredicate;

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
    public void parse_typedTerms_returnsTypedFindCommand() {
        var expected = new FindCommand(new TypedSearchPredicate(List.of("Alex", "Sam"), List.of("hackathon"),
                List.of("teammate"), List.of("Hack Singapore", "AI")));
        assertParseSuccess(parser, " n/Alex n/Sam t/hackathon nt/teammate ev/Hack Singapore ev/AI ", expected);
    }

    @Test
    public void parse_slashContainingEvent_preservesEventText() {
        for (String event : new String[]{"Conference c/o NUS", "ai/ml workshop", "Conference https://nus.edu.sg"}) {
            assertParseSuccess(parser, " ev/" + event,
                    new FindCommand(new TypedSearchPredicate(List.of(), List.of(), List.of(), List.of(event))));
        }
    }

    @Test
    public void parse_emptyMalformedOrMixedSearch_rejectsCorrectMessage() {
        for (String args : new String[]{" n/", " ev/Hack ev/", " p/91234567"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        assertParseFailure(parser, " Alice n/Hack", FindCommand.MESSAGE_MIXED_SEARCH);
    }

}
