package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person} has a follow-up date on or before {@code today}.
 */
public class FollowUpDuePredicate implements Predicate<Person> {
    private final LocalDate today;

    public FollowUpDuePredicate(LocalDate today) {
        this.today = requireNonNull(today);
    }

    @Override
    public boolean test(Person person) {
        return person.isFollowUpDueOn(today);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof FollowUpDuePredicate otherPredicate && today.equals(otherPredicate.today));
    }

    @Override
    public int hashCode() {
        return today.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("today", today).toString();
    }
}
