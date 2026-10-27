package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class MeetingTest {
    @Test
    public void isValidText_absentBlankAndBoundaryValues_validatesPresentText() {
        for (String value : new String[]{null, "", " ", "\t\n", "\u2003", "x".repeat(81)}) {
            assertFalse(Meeting.isValidText(value));
        }
        for (String value : new String[]{"x", " x ", "x".repeat(80), "😀".repeat(80)}) {
            assertTrue(Meeting.isValidText(value));
        }
    }

    @Test
    public void isValidText_combiningMarksAndJoinedEmoji_countsEachCodePoint() {
        for (String unit : new String[]{"e\u0301", "👩\u200D💻"}) {
            int pointsPerUnit = unit.codePointCount(0, unit.length());
            String atLimit = unit.repeat(80 / pointsPerUnit) + "x".repeat(80 % pointsPerUnit);
            assertTrue(Meeting.isValidText(atLimit));
            assertFalse(Meeting.isValidText(atLimit + "x"));
            assertEquals(atLimit, new Meeting(atLimit, null, atLimit).getEventName().orElseThrow());
        }
    }

    @Test
    public void constructor_invalidText_explainsCodePointLimit() {
        for (String value : new String[]{"", " ", "😀".repeat(81)}) {
            var eventError = assertThrows(IllegalArgumentException.class, () -> new Meeting(value, null, null));
            var roleError = assertThrows(IllegalArgumentException.class, () -> new Meeting(null, null, value));
            String expected = "Event and role must each be nonblank and contain 1 to 80 Unicode code points.";
            assertEquals(expected, eventError.getMessage());
            assertEquals(expected, roleError.getMessage());
        }
    }

    @Test
    public void constructor_supplementaryCharacters_countsCodePoints() {
        String atLimit = "😀".repeat(80);
        Meeting meeting = new Meeting(atLimit, null, atLimit);
        assertEquals(atLimit, meeting.getEventName().orElseThrow());
        assertEquals(atLimit, meeting.getRole().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> new Meeting(atLimit + "a", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Meeting(null, null, atLimit + "a"));
    }

    @Test
    public void constructor_validatesTextAndRealCalendarDates() {
        assertEquals(LocalDate.of(2024, 2, 29), Meeting.parseDate("2024-02-29"));
        for (String date : new String[]{"2026-02-29", "2026-02-30", "2026-2-01", "0000-01-01"}) {
            assertThrows(IllegalArgumentException.class, () -> Meeting.parseDate(date));
        }
        assertThrows(IllegalArgumentException.class, () -> new Meeting(" ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Meeting("x".repeat(81), null, null));
        assertThrows(IllegalArgumentException.class, () -> new Meeting(null, null, "x".repeat(81)));
        assertEquals(80, new Meeting("x".repeat(80), null, null).getEventName().orElseThrow().length());
        assertTrue(Meeting.EMPTY.isEmpty());
    }

    @Test
    public void personCopy_preservesMeetingAndIncludesItInEquality() {
        Person original = new PersonBuilder().build();
        Meeting meeting = new Meeting("Hackathon", LocalDate.of(2026, 10, 20), "Mentor");
        Person updated = original.toBuilder().withMeeting(meeting).build();
        assertEquals(meeting, updated.toBuilder().withPhone(original.getPhone()).build().getMeeting());
        assertEquals(updated, updated.toBuilder().build());
        assertEquals(updated.hashCode(), updated.toBuilder().build().hashCode());
        assertTrue(!original.hasSameDetails(updated));
    }

    @Test
    public void constructor_dateBoundariesAndBlankRole_validatesOptionalFields() {
        assertThrows(IllegalArgumentException.class, () -> Meeting.parseDate(null));
        for (int year : new int[]{0, -1, 10000}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Meeting(null, LocalDate.of(year, 1, 1), null));
        }
        assertThrows(IllegalArgumentException.class, () -> new Meeting(null, null, " "));
        for (int year : new int[]{1, 9999}) {
            LocalDate date = LocalDate.of(year, 1, 1);
            assertEquals(date, new Meeting(null, date, null).getDate().orElseThrow());
        }
        assertEquals(80, new Meeting(null, null, "x".repeat(80)).getRole().orElseThrow().length());
    }

    @Test
    public void equals_optionalFieldsAndOtherTypes_comparesAllContext() {
        Meeting full = new Meeting("Hack", LocalDate.of(2026, 10, 20), "Mentor");
        assertEquals(full, new Meeting("Hack", LocalDate.of(2026, 10, 20), "Mentor"));
        assertEquals(full.hashCode(), new Meeting("Hack", LocalDate.of(2026, 10, 20), "Mentor").hashCode());
        for (Object other : new Object[]{null, "Hack", Meeting.EMPTY,
            new Meeting("Other", full.getDate().orElseThrow(), "Mentor"),
            new Meeting("Hack", null, "Mentor"), new Meeting("Hack", full.getDate().orElseThrow(), null)}) {
            assertNotEquals(full, other);
        }
        assertFalse(new Meeting(null, LocalDate.of(2026, 10, 20), null).isEmpty());
        assertFalse(new Meeting(null, null, "Mentor").isEmpty());
        assertEquals("Meeting[event=Hack, date=2026-10-20, role=Mentor]", full.toString());
    }

}
