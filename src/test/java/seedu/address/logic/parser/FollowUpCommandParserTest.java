package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.model.person.FollowUpDate;

public class FollowUpCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            FollowUpCommand.MESSAGE_USAGE);

    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    @Test
    public void parse_validDate_returnsFollowUpCommand() {
        FollowUpCommand expected = new FollowUpCommand(INDEX_FIRST_PERSON, new FollowUpDate("2026-10-25"));

        assertParseSuccess(parser, " 1 d/2026-10-25", expected);
        assertParseSuccess(parser, "1 d/2026-10-25", expected);
        assertParseSuccess(parser, "  1   d/  2026-10-25  ", expected);
    }

    @Test
    public void parse_otherIndex_returnsFollowUpCommand() {
        assertParseSuccess(parser, "2 d/2026-10-25",
                new FollowUpCommand(INDEX_SECOND_PERSON, new FollowUpDate("2026-10-25")));
    }

    @Test
    public void parse_pastDate_returnsFollowUpCommand() {
        assertParseSuccess(parser, "1 d/2020-01-01",
                new FollowUpCommand(INDEX_FIRST_PERSON, new FollowUpDate("2020-01-01")));
    }

    @Test
    public void parse_emptyDate_returnsClearingCommand() {
        FollowUpCommand expected = new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE);

        assertParseSuccess(parser, "1 d/", expected);
        assertParseSuccess(parser, "1 d/   ", expected);
    }

    @Test
    public void parse_missingDatePrefix_throwsInvalidCommandFormat() {
        assertParseFailure(parser, "1", INVALID_FORMAT);
        assertParseFailure(parser, "1 ", INVALID_FORMAT);
        assertParseFailure(parser, "1 2026-10-25", INVALID_FORMAT);
    }

    @Test
    public void parse_missingIndex_throwsInvalidCommandFormat() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, " d/2026-10-25", INVALID_FORMAT);
        assertParseFailure(parser, "d/", INVALID_FORMAT);
    }

    @Test
    public void parse_invalidDate_throwsDateConstraints() {
        String[] invalidDates = {"tomorrow", "2026-02-30", "2026-13-01", "25-10-2026", "2026/10/25", "2026-1-5",
            "20261025", "2026-10-25 10:00"};

        for (String invalidDate : invalidDates) {
            assertParseFailure(parser, "1 d/" + invalidDate, FollowUpDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidIndex_throwsInvalidPersonIndex() {
        String[] invalidIndexes = {"0", "-1", "abc", "1.5", "1 2", "999999999999"};

        for (String invalidIndex : invalidIndexes) {
            assertParseFailure(parser, invalidIndex + " d/2026-10-25", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_duplicateDatePrefix_throwsDuplicatePrefixMessage() {
        assertParseFailure(parser, "1 d/2026-10-25 d/2026-10-26", getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }
}
