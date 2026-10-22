package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Matches typed search terms across contact fields.
 * Terms within one field use OR; terms from different fields use AND.
 */
public final class TypedSearchPredicate implements Predicate<Person> {
    private final List<String> nameTerms;
    private final List<String> tagTerms;
    private final List<String> noteTerms;
    private final List<String> eventTerms;

    /** Creates a predicate from the typed terms for each searchable contact field. */
    public TypedSearchPredicate(List<String> nameTerms, List<String> tagTerms, List<String> noteTerms,
            List<String> eventTerms) {
        this.nameTerms = List.copyOf(nameTerms);
        this.tagTerms = List.copyOf(tagTerms);
        this.noteTerms = List.copyOf(noteTerms);
        this.eventTerms = List.copyOf(eventTerms);
    }

    @Override
    public boolean test(Person person) {
        return matchesAny(nameTerms, person.getName().fullName)
                && matchesAnyTag(person)
                && matchesAny(noteTerms, person.getNote().value)
                && matchesEvent(person);
    }

    private boolean matchesAnyTag(Person person) {
        return tagTerms.isEmpty() || person.getTags().stream()
                .anyMatch(tag -> matchesAny(tagTerms, tag.tagName));
    }

    private boolean matchesEvent(Person person) {
        return person.getMeeting().getEventName()
                .map(event -> matchesAny(eventTerms, event))
                .orElse(eventTerms.isEmpty());
    }

    private static boolean matchesAny(List<String> terms, String text) {
        String lowerCaseText = text.toLowerCase(Locale.ROOT);
        return terms.isEmpty() || terms.stream()
                .map(term -> term.toLowerCase(Locale.ROOT))
                .anyMatch(lowerCaseText::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypedSearchPredicate predicate)) {
            return false;
        }
        return nameTerms.equals(predicate.nameTerms)
                && tagTerms.equals(predicate.tagTerms)
                && noteTerms.equals(predicate.noteTerms)
                && eventTerms.equals(predicate.eventTerms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameTerms, tagTerms, noteTerms, eventTerms);
    }
}
