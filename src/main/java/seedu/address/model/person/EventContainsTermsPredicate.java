package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Matches any supplied partial event term, ignoring case. Reusable by the shared search pipeline. */
public final class EventContainsTermsPredicate implements Predicate<Person> {
    private final List<String> terms;

    /** Copies the supplied event terms to keep filter behavior stable after construction. */
    public EventContainsTermsPredicate(List<String> terms) {
        this.terms = List.copyOf(terms);
    }

    @Override
    public boolean test(Person person) {
        return person.getMeeting().getEventName().map(event -> terms.stream()
                .anyMatch(term -> event.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT))))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EventContainsTermsPredicate predicate && terms.equals(predicate.terms);
    }

    @Override
    public int hashCode() {
        return terms.hashCode();
    }
}
