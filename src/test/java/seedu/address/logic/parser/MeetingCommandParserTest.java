package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MeetingCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Meeting;

public class MeetingCommandParserTest {

    private final MeetingCommandParser parser = new MeetingCommandParser();

    private static String format(String hint) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, hint);
    }

    /*
     * The word clear comes after the index. A command that starts with it has no index, or has the words in the
     * wrong order, so it reports the usage and not an invalid index.
     */
    @Test
    public void parse_clearInPlaceOfTheIndex_throwsUsageAndNotInvalidIndex() {
        assertParseFailure(parser, " clear", format(MeetingCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " CLEAR", format(MeetingCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " clear 1", format(MeetingCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " clear date", format(MeetingCommand.MESSAGE_MISSING_INDEX));
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

    @Test
    public void parse_fieldNameInAnyLetterCase_clearsOnlyThatField() throws Exception {
        Meeting full = new Meeting("Hack Singapore", LocalDate.of(2026, 10, 25), "Mentor");

        assertEquals(new Meeting(null, full.getDate().get(), "Mentor"), meetingAfter(" 1 clear event", full));
        assertEquals(new Meeting("Hack Singapore", null, "Mentor"), meetingAfter(" 1 clear date", full));
        assertEquals(new Meeting("Hack Singapore", full.getDate().get(), null), meetingAfter(" 1 clear role", full));
        // MeetingCommand has no equals, so compare what each spelling does to the same meeting
        for (String field : new String[]{"event", "date", "role"}) {
            String capitalised = Character.toUpperCase(field.charAt(0)) + field.substring(1);
            assertEquals(meetingAfter(" 1 clear " + field, full), meetingAfter(" 1 clear " + capitalised, full));
            assertEquals(meetingAfter(" 1 clear " + field, full),
                    meetingAfter(" 1 CLEAR " + field.toUpperCase(), full));
        }
    }

    @Test
    public void parse_unknownFieldName_throwsUsage() {
        assertParseFailure(parser, " 1 clear DATES", format(MeetingCommand.MESSAGE_CLEAR_USAGE));
        assertParseFailure(parser, " 1 clear when", format(MeetingCommand.MESSAGE_CLEAR_USAGE));
    }

    @Test
    public void parse_invalidFields_identifiesTheCorrectionWithoutEchoingInput() {
        assertParseFailure(parser, " 1 ev/", Meeting.MESSAGE_EVENT);
        assertParseFailure(parser, " 1 ev/" + "x".repeat(81), Meeting.MESSAGE_EVENT);
        assertParseFailure(parser, " 1 r/", Meeting.MESSAGE_ROLE);
        assertParseFailure(parser, " 1 r/" + "x".repeat(81), Meeting.MESSAGE_ROLE);
        assertParseFailure(parser, " 1 md/2026-02-30", MeetingCommandParser.MESSAGE_DATE);
        assertParseFailure(parser, " 1 md/", MeetingCommandParser.MESSAGE_DATE);
        assertParseFailure(parser, " 1", format(MeetingCommand.MESSAGE_NO_UPDATE));
    }

    @Test
    public void parse_wrongShape_givesOneSpecificHint() {
        assertParseFailure(parser, "", format(MeetingCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " ev/Event", format(MeetingCommand.MESSAGE_MISSING_INDEX));
        assertParseFailure(parser, " 1 clear ev/Event", format(MeetingCommand.MESSAGE_NOT_BOTH));
        assertParseFailure(parser, " 1 Mentor", format(MeetingCommand.MESSAGE_WITHOUT_PREFIX));
        assertParseFailure(parser, " 1 2 ev/Event", format(MeetingCommand.MESSAGE_EXTRA_WORDS));
        assertParseFailure(parser, " 1 clear event extra", format(MeetingCommand.MESSAGE_CLEAR_USAGE));
    }

    @Test
    public void parse_dateAndTextFailures_keepTheirOwnHints() {
        assertParseFailure(parser, " 1 md/2026-02-30 ev/Event", MeetingCommandParser.MESSAGE_DATE);
        assertParseFailure(parser, " 1 md/2024-02-29 ev/", Meeting.MESSAGE_EVENT);
        assertParseFailure(parser, " 1 md/2024-02-29 r/", Meeting.MESSAGE_ROLE);
    }

    /** Returns the meeting of the only contact after running {@code args}, starting from {@code meeting}. */
    private Meeting meetingAfter(String args, Meeting meeting) throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(ALICE.toBuilder().withMeeting(meeting).build());
        parser.parse(args).execute(model);
        return model.getFilteredPersonList().get(0).getMeeting();
    }
}
