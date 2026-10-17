package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FollowUpDuePredicateTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);

    private final FollowUpDuePredicate predicate = new FollowUpDuePredicate(TODAY);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpDuePredicate(null));
    }

    @Test
    public void test_dateBeforeToday_returnsTrue() {
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("2026-10-20").build()));
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("2020-01-01").build()));
    }

    @Test
    public void test_dateIsToday_returnsTrue() {
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("2026-10-21").build()));
    }

    @Test
    public void test_dateAfterToday_returnsFalse() {
        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate("2026-10-22").build()));
    }

    @Test
    public void test_noDate_returnsFalse() {
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void equals() {
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new FollowUpDuePredicate(TODAY)));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(5));
        assertFalse(predicate.equals(new FollowUpDuePredicate(TODAY.plusDays(1))));
    }

    @Test
    public void toStringMethod() {
        assertEquals(FollowUpDuePredicate.class.getCanonicalName() + "{today=" + TODAY + "}", predicate.toString());
    }
}
