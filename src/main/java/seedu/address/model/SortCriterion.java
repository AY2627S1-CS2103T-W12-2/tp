package seedu.address.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import seedu.address.model.person.Person;

/**
 * The order of the visible contact list: an activity timestamp, or the follow-up date.
 */
public enum SortCriterion {
    ADDED("added", newestFirst(person -> person.getActivity().getCreatedAt())),
    EDITED("edited", newestFirst(person -> person.getActivity().getLastEditedAt())),
    VIEWED("viewed", newestFirst(person -> person.getActivity().getLastViewedAt())),
    /** Set by the {@code due} command only; it is not a {@code sort} argument. */
    DUE("due", Comparator.comparing(person -> person.getFollowUpDate().orElse(null),
            Comparator.nullsLast(Comparator.<LocalDate>naturalOrder())));

    private final String commandArgument;
    private final Comparator<Person> comparator;

    SortCriterion(String commandArgument, Comparator<Person> comparator) {
        this.commandArgument = commandArgument;
        this.comparator = comparator;
    }

    /** Orders known timestamps newest first and unknown timestamps last. */
    private static Comparator<Person> newestFirst(Function<Person, Optional<Instant>> timestampExtractor) {
        return Comparator.comparing(person -> timestampExtractor.apply(person).orElse(null),
                Comparator.nullsLast(Comparator.<Instant>reverseOrder()));
    }

    /**
     * Returns the command argument used to select this criterion.
     */
    public String getCommandArgument() {
        return commandArgument;
    }

    /**
     * Returns the comparator for this criterion. Timestamps are newest first with unknown times last. The
     * follow-up date is earliest first with no date last.
     */
    public Comparator<Person> getComparator() {
        return comparator;
    }
}
