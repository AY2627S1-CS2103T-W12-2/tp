package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class EventContainsTermsPredicateTest {
    @Test
    public void test_partialCaseInsensitiveTerms_matchesAnyTermOnlyInEvent() {
        var person = new PersonBuilder().build().toBuilder()
                .withMeeting(new Meeting("Hack Singapore", null, "Mentor")).build();
        assertTrue(new EventContainsTermsPredicate(List.of("absent", "sInGa")).test(person));
        assertFalse(new EventContainsTermsPredicate(List.of("Mentor")).test(person));
        assertFalse(new EventContainsTermsPredicate(List.of()).test(person));
        assertFalse(new EventContainsTermsPredicate(List.of("Hack")).test(new PersonBuilder().build()));
    }

    @Test
    public void constructor_copiesTermsAndSupportsValueEquality() {
        var terms = new ArrayList<>(List.of("Hack"));
        var predicate = new EventContainsTermsPredicate(terms);
        terms.clear();
        var same = new EventContainsTermsPredicate(List.of("Hack"));
        assertEquals(same, predicate);
        assertEquals(same.hashCode(), predicate.hashCode());
        assertNotEquals(new EventContainsTermsPredicate(List.of("Other")), predicate);
        assertFalse(predicate.equals(null));
        assertNotEquals("Hack", predicate);
    }
}
