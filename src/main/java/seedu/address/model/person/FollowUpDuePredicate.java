package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person} has a follow-up date that is today or earlier.
 */
public class FollowUpDuePredicate implements Predicate<Person> {

    private final LocalDate today;

    /**
     * Creates a predicate that accepts persons whose follow-up date is {@code today} or earlier.
     */
    public FollowUpDuePredicate(LocalDate today) {
        requireNonNull(today);
        this.today = today;
    }

    @Override
    public boolean test(Person person) {
        return person.getFollowUpDate().isDueOn(today);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FollowUpDuePredicate otherPredicate)) {
            return false;
        }

        return today.equals(otherPredicate.today);
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
