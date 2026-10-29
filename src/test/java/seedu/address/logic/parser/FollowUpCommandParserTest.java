package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpCommand;

public class FollowUpCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            FollowUpCommand.MESSAGE_USAGE);

    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    /*
     * The word clear comes after the index. A command that starts with it has no index, or has the words in the
     * wrong order, so it reports the usage and not an invalid index.
     */
    @Test
    public void parse_clearInPlaceOfTheIndex_throwsUsageAndNotInvalidIndex() {
        assertParseFailure(parser, " clear", INVALID_FORMAT);
        assertParseFailure(parser, " CLEAR", INVALID_FORMAT);
        assertParseFailure(parser, " clear 1", INVALID_FORMAT);
        assertParseFailure(parser, " Clear 1 d/2026-10-25", INVALID_FORMAT);
    }

    @Test
    public void parse_wrongIndexBeforeClear_stillThrowsInvalidIndex() {
        assertParseFailure(parser, " 0 clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, " abc clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_setDate_success() {
        assertParseSuccess(parser, " 2 d/2026-10-25",
                new FollowUpCommand(INDEX_SECOND_PERSON, LocalDate.of(2026, 10, 25)));
    }

    @Test
    public void parse_leapDay_success() {
        assertParseSuccess(parser, "1 d/2028-02-29",
                new FollowUpCommand(INDEX_FIRST_PERSON, LocalDate.of(2028, 2, 29)));
    }

    @Test
    public void parse_clear_success() {
        assertParseSuccess(parser, " 1 clear", new FollowUpCommand(INDEX_FIRST_PERSON, null));
        assertParseSuccess(parser, " 1 CLEAR", new FollowUpCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void parse_invalidDate_failure() {
        assertParseFailure(parser, "1 d/2026-02-30", FollowUpCommand.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 d/2027-02-29", FollowUpCommand.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 d/2026-1-5", FollowUpCommand.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 d/25-10-2026", FollowUpCommand.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 d/tomorrow", FollowUpCommand.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 d/", FollowUpCommand.MESSAGE_INVALID_DATE);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 d/2026-10-25", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "-1 clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "x d/2026-10-25", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_badFormat_failure() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "1", INVALID_FORMAT);
        assertParseFailure(parser, " d/2026-10-25", INVALID_FORMAT);
        assertParseFailure(parser, "1 clear now", INVALID_FORMAT);
        assertParseFailure(parser, "1 clear d/2026-10-25", INVALID_FORMAT);
        assertParseFailure(parser, "1 2026-10-25", INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateDate_failure() {
        assertParseFailure(parser, "1 d/2026-10-25 d/2026-10-26",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_FOLLOW_UP_DATE));
    }
}
