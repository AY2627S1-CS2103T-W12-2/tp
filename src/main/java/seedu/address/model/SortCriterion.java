package seedu.address.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import seedu.address.model.person.Person;

/**
 * The activity timestamp used to order the visible contact list.
 */
public enum SortCriterion {
    ADDED("added", person -> person.getActivity().getCreatedAt()),
    EDITED("edited", person -> person.getActivity().getLastEditedAt()),
    VIEWED("viewed", person -> person.getActivity().getLastViewedAt());

    private final String commandArgument;
    private final Function<Person, Optional<Instant>> timestampExtractor;

    SortCriterion(String commandArgument, Function<Person, Optional<Instant>> timestampExtractor) {
        this.commandArgument = commandArgument;
        this.timestampExtractor = timestampExtractor;
    }

    /**
     * Returns the command argument used to select this criterion.
     */
    public String getCommandArgument() {
        return commandArgument;
    }

    /**
     * Returns a comparator that orders known timestamps newest first and unknown timestamps last.
     */
    public Comparator<Person> getComparator() {
        return Comparator.comparing(person -> timestampExtractor.apply(person).orElse(null),
                Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
