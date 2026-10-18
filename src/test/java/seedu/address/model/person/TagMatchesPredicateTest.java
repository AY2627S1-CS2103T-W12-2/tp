package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class TagMatchesPredicateTest {

    @Test
    public void equals() {
        TagMatchesPredicate firstPredicate = new TagMatchesPredicate(new Tag("react"));
        TagMatchesPredicate secondPredicate = new TagMatchesPredicate(new Tag("figma"));

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new TagMatchesPredicate(new Tag("REACT"))));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals("react"));
        assertEquals(firstPredicate.hashCode(), new TagMatchesPredicate(new Tag("react")).hashCode());
    }

    @Test
    public void test_personWithMatchingTag_returnsTrue() {
        TagMatchesPredicate predicate = new TagMatchesPredicate(new Tag("REACT"));
        Person person = new PersonBuilder().withTags("react", "backend").build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_personWithoutMatchingTag_returnsFalse() {
        TagMatchesPredicate predicate = new TagMatchesPredicate(new Tag("react"));
        Person personWithoutTags = new PersonBuilder().withTags().build();
        Person personWithDifferentTag = new PersonBuilder().withTags("figma").build();

        assertFalse(predicate.test(personWithoutTags));
        assertFalse(predicate.test(personWithDifferentTag));
    }
}
