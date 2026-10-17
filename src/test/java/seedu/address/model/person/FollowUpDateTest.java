package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FollowUpDateTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpDate(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("2026-02-30"));
    }

    @Test
    public void isValidFollowUpDate() {
        // null
        assertThrows(NullPointerException.class, () -> FollowUpDate.isValidFollowUpDate(null));

        // invalid
        assertFalse(FollowUpDate.isValidFollowUpDate(" ")); // spaces only
        assertFalse(FollowUpDate.isValidFollowUpDate("tomorrow")); // words
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-02-30")); // no such day
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-02-29")); // 2026 is not a leap year
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-13-01")); // no such month
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-00-10")); // month zero
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-10-00")); // day zero
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-1-5")); // not zero padded
        assertFalse(FollowUpDate.isValidFollowUpDate("26-10-25")); // two digit year
        assertFalse(FollowUpDate.isValidFollowUpDate("2026/10/25")); // wrong separator
        assertFalse(FollowUpDate.isValidFollowUpDate("25-10-2026")); // day first
        assertFalse(FollowUpDate.isValidFollowUpDate("20261025")); // no separator
        assertFalse(FollowUpDate.isValidFollowUpDate(" 2026-10-25")); // leading space
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-10-25 ")); // trailing space
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-10-25T10:00")); // has a time
        assertFalse(FollowUpDate.isValidFollowUpDate("2026-10-25x")); // trailing text
        assertFalse(FollowUpDate.isValidFollowUpDate("+12026-10-25")); // five digit year

        // valid
        assertTrue(FollowUpDate.isValidFollowUpDate("")); // no date
        assertTrue(FollowUpDate.isValidFollowUpDate("2026-10-25"));
        assertTrue(FollowUpDate.isValidFollowUpDate("2028-02-29")); // leap day
        assertTrue(FollowUpDate.isValidFollowUpDate("2026-12-31"));
        assertTrue(FollowUpDate.isValidFollowUpDate("2020-01-01")); // past dates are allowed
    }

    @Test
    public void none_hasNoDate() {
        assertTrue(FollowUpDate.NONE.isEmpty());
        assertEquals(Optional.empty(), FollowUpDate.NONE.getDate());
        assertEquals("", FollowUpDate.NONE.value);
        assertEquals(FollowUpDate.NONE, new FollowUpDate(""));
    }

    @Test
    public void getDate_validDate_returnsLocalDate() {
        FollowUpDate followUpDate = new FollowUpDate("2026-10-25");

        assertFalse(followUpDate.isEmpty());
        assertEquals(Optional.of(LocalDate.of(2026, 10, 25)), followUpDate.getDate());
        assertEquals("2026-10-25", followUpDate.value);
    }

    @Test
    public void isDueOn() {
        assertTrue(new FollowUpDate("2026-10-20").isDueOn(TODAY)); // yesterday
        assertTrue(new FollowUpDate("2026-10-21").isDueOn(TODAY)); // today
        assertFalse(new FollowUpDate("2026-10-22").isDueOn(TODAY)); // tomorrow
        assertTrue(new FollowUpDate("2020-01-01").isDueOn(TODAY)); // long ago
        assertFalse(FollowUpDate.NONE.isDueOn(TODAY));
    }

    @Test
    public void isOverdueOn() {
        assertTrue(new FollowUpDate("2026-10-20").isOverdueOn(TODAY)); // yesterday
        assertFalse(new FollowUpDate("2026-10-21").isOverdueOn(TODAY)); // due today is not yet overdue
        assertFalse(new FollowUpDate("2026-10-22").isOverdueOn(TODAY)); // tomorrow
        assertFalse(FollowUpDate.NONE.isOverdueOn(TODAY));
    }

    @Test
    public void equals() {
        FollowUpDate followUpDate = new FollowUpDate("2026-10-25");

        assertTrue(followUpDate.equals(new FollowUpDate("2026-10-25")));
        assertTrue(followUpDate.equals(followUpDate));
        assertFalse(followUpDate.equals(null));
        assertFalse(followUpDate.equals(5));
        assertFalse(followUpDate.equals(new FollowUpDate("2026-10-26")));
        assertFalse(followUpDate.equals(FollowUpDate.NONE));
    }

    @Test
    public void hashCode_equalDates_sameHash() {
        assertEquals(new FollowUpDate("2026-10-25").hashCode(), new FollowUpDate("2026-10-25").hashCode());
    }

    @Test
    public void toString_returnsIsoDate() {
        assertEquals("2026-10-25", new FollowUpDate("2026-10-25").toString());
        assertEquals("", FollowUpDate.NONE.toString());
    }
}
