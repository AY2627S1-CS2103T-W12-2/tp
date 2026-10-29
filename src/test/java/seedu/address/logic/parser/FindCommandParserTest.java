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
    public void parse_tagOnly_returnsTypedFindCommand() {
        assertParseSuccess(parser, " t/react",
                new FindCommand(new TypedSearchPredicate(List.of(), List.of("react"), List.of(), List.of())));
    }

    @Test
    public void parse_slashContainingEvent_preservesEventText() {
        for (String event : new String[]{"Conference c/o NUS", "ai/ml workshop", "Conference https://nus.edu.sg"}) {
            assertParseSuccess(parser, " ev/" + event,
                    new FindCommand(new TypedSearchPredicate(List.of(), List.of(), List.of(), List.of(event))));
        }
    }

    @Test
    public void parse_unsupportedCommandFields_rejectsRegardlessOfPosition() {
        for (String field : List.of("e/p0@example.com", "p/90000000", "l/user-0x", "d/2026-10-28",
                "md/2026-10-28", "r/developer", "f/photo.png")) {
            for (String args : List.of(" " + field, " " + field + " n/aisha",
                    " n/aisha " + field, " n/aisha " + field + " t/react")) {
                assertParseFailure(parser, args,
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
        }
    }

    @Test
    public void parse_unsupportedFieldAfterAnySearchField_rejectsFormat() {
        for (String prefix : List.of("n/", "t/", "nt/", "ev/")) {
            for (String separator : List.of(" ", "\t", "\n")) {
                assertParseFailure(parser, " " + prefix + "text" + separator + "e/",
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
        }
    }

    @Test
    public void parse_slashesWithinNoteText_preservesText() {
        String note = "c/o NUS ai/ml https://example.com/e/user path/p/90000000";
        assertParseSuccess(parser, " nt/" + note,
                new FindCommand(new TypedSearchPredicate(List.of(), List.of(), List.of(note), List.of())));
    }

    @Test
    public void parse_reservedPrefixInNoteOrEventText_rejectsFormat() {
        for (String args : List.of(" nt/likes r/java", " ev/likes r/java")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_spaceInsideSearchPrefix_rejectsFormat() {
        for (String prefix : List.of("n", "t", "nt", "ev")) {
            for (String gap : List.of(" ", "\t", "  ")) {
                for (String args : List.of(" " + prefix + gap + "/Alice",
                        " n/Alice " + prefix + gap + "/react")) {
                    assertParseFailure(parser, args,
                            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
                }
            }
        }
    }

    @Test
    public void parse_emptyMalformedOrMixedSearch_rejectsCorrectMessage() {
        for (String args : new String[]{" n/", " t/", " nt/", " ev/Hack ev/", " p/91234567", " unknown/value"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        assertParseFailure(parser, " Alice n/Hack", FindCommand.MESSAGE_MIXED_SEARCH);
    }

}
