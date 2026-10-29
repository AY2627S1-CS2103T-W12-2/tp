package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TypedSearchPredicateTest {
    private final Person contact = new PersonBuilder().withName("Alex Tan").withTags("Hackathon", "frontend")
            .withNote("Looking for a React teammate").build().toBuilder()
            .withMeeting(new Meeting("NUS Hack Singapore", null, null)).build();

    @Test
    public void test_sameFieldTermsUseOrAndDifferentFieldsUseAnd() {
        assertTrue(new TypedSearchPredicate(List.of("sam", "AL"), List.of("hack"), List.of("react"),
                List.of("singapore")).test(contact));
        assertFalse(new TypedSearchPredicate(List.of("alex"), List.of("backend"), List.of(), List.of())
                .test(contact));
    }

    @Test
    public void test_nonEnglishNamesMatchPartOfTheNameIgnoringCase() {
        PersonBuilder jose = new PersonBuilder().withName("José Tan");
        PersonBuilder liMing = new PersonBuilder().withName("李明");

        assertTrue(new TypedSearchPredicate(List.of("OSÉ"), List.of(), List.of(), List.of()).test(jose.build()));
        assertTrue(new TypedSearchPredicate(List.of("李"), List.of(), List.of(), List.of()).test(liMing.build()));
        assertFalse(new TypedSearchPredicate(List.of("李"), List.of(), List.of(), List.of()).test(jose.build()));
    }

    @Test
    public void test_missingOptionalFieldsDoNotMatchRequestedTerms() {
        Person withoutOptionalFields = new PersonBuilder().withName("Alex Tan").build();

        assertFalse(new TypedSearchPredicate(List.of(), List.of(), List.of("react"), List.of())
                .test(withoutOptionalFields));
        assertFalse(new TypedSearchPredicate(List.of(), List.of(), List.of(), List.of("hack"))
                .test(withoutOptionalFields));
    }

    @Test
    public void equals_andHashCode() {
        TypedSearchPredicate predicate = new TypedSearchPredicate(List.of("alex"), List.of("hack"),
                List.of("react"), List.of("singapore"));
        TypedSearchPredicate samePredicate = new TypedSearchPredicate(List.of("alex"), List.of("hack"),
                List.of("react"), List.of("singapore"));

        assertEquals(predicate, predicate);
        assertEquals(predicate, samePredicate);
        assertEquals(predicate.hashCode(), samePredicate.hashCode());
        assertNotEquals(predicate, new TypedSearchPredicate(List.of("sam"), List.of("hack"),
                List.of("react"), List.of("singapore")));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "search");
    }
}
