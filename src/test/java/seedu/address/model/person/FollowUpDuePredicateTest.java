package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FollowUpDuePredicateTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);

    @Test
    public void test_dueOnOrBeforeToday_true() {
        FollowUpDuePredicate predicate = new FollowUpDuePredicate(TODAY);

        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate(TODAY.minusDays(30)).build()));
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate(TODAY.minusDays(1)).build()));
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate(TODAY).build()));
    }

    @Test
    public void test_afterTodayOrNoDate_false() {
        FollowUpDuePredicate predicate = new FollowUpDuePredicate(TODAY);

        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate(TODAY.plusDays(1)).build()));
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpDuePredicate(null));
    }

    @Test
    public void equals() {
        FollowUpDuePredicate predicate = new FollowUpDuePredicate(TODAY);

        assertEquals(predicate, new FollowUpDuePredicate(TODAY));
        assertEquals(predicate.hashCode(), new FollowUpDuePredicate(TODAY).hashCode());
        assertNotEquals(predicate, new FollowUpDuePredicate(TODAY.plusDays(1)));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "x");
    }
}
