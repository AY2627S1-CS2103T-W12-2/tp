package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match phone and email, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").build()));
    }

    @Test
    public void test_hyphenatedName_isOneWord() {
        PersonBuilder jane = new PersonBuilder().withName("Mary-Jane O'Brien");

        // the whole hyphenated or apostrophe word must be given
        assertTrue(new NameContainsKeywordsPredicate(List.of("mary-jane")).test(jane.build()));
        assertTrue(new NameContainsKeywordsPredicate(List.of("O'Brien")).test(jane.build()));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Mary")).test(jane.build()));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Brien")).test(jane.build()));
    }

    @Test
    public void test_accentedName_matchesWholeWordWithItsAccent() {
        PersonBuilder jose = new PersonBuilder().withName("José Tan");

        assertTrue(new NameContainsKeywordsPredicate(List.of("José")).test(jose.build()));
        assertTrue(new NameContainsKeywordsPredicate(List.of("JOSÉ")).test(jose.build()));
        // the accent counts: a search without it does not find the name
        assertFalse(new NameContainsKeywordsPredicate(List.of("Jose")).test(jose.build()));
    }

    @Test
    public void test_keywordWithSeparateAccent_matchesTheComposedName() {
        PersonBuilder jose = new PersonBuilder().withName("José Tan");

        assertTrue(new NameContainsKeywordsPredicate(List.of("José")).test(jose.build()));
        // a name stored from a separate accent is the same name
        assertTrue(new NameContainsKeywordsPredicate(List.of("José"))
                .test(new PersonBuilder().withName("José Tan").build()));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Jose")).test(jose.build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
