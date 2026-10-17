package seedu.address.model.person;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents when a {@code Person} was created, last edited and last viewed.
 * Each time is optional: a time that is not known (for example, for contacts saved before activity was
 * tracked) is absent rather than guessed.
 * Guarantees: immutable.
 */
public class Activity {

    /** An activity with no known times. */
    public static final Activity EMPTY = new Activity(null, null, null);

    private final Instant createdAt;
    private final Instant lastEditedAt;
    private final Instant lastViewedAt;

    /**
     * Any of the times may be null, which means the time is not known.
     */
    public Activity(Instant createdAt, Instant lastEditedAt, Instant lastViewedAt) {
        this.createdAt = createdAt;
        this.lastEditedAt = lastEditedAt;
        this.lastViewedAt = lastViewedAt;
    }

    public Optional<Instant> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<Instant> getLastEditedAt() {
        return Optional.ofNullable(lastEditedAt);
    }

    public Optional<Instant> getLastViewedAt() {
        return Optional.ofNullable(lastViewedAt);
    }

    /**
     * Returns a copy of this activity with {@code createdAt} replaced.
     */
    public Activity withCreatedAt(Instant createdAt) {
        return new Activity(createdAt, lastEditedAt, lastViewedAt);
    }

    /**
     * Returns a copy of this activity with {@code lastEditedAt} replaced.
     */
    public Activity withLastEditedAt(Instant lastEditedAt) {
        return new Activity(createdAt, lastEditedAt, lastViewedAt);
    }

    /**
     * Returns a copy of this activity with {@code lastViewedAt} replaced.
     */
    public Activity withLastViewedAt(Instant lastViewedAt) {
        return new Activity(createdAt, lastEditedAt, lastViewedAt);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Activity otherActivity)) {
            return false;
        }

        return Objects.equals(createdAt, otherActivity.createdAt)
                && Objects.equals(lastEditedAt, otherActivity.lastEditedAt)
                && Objects.equals(lastViewedAt, otherActivity.lastViewedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, lastEditedAt, lastViewedAt);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("createdAt", createdAt)
                .add("lastEditedAt", lastEditedAt)
                .add("lastViewedAt", lastViewedAt)
                .toString();
    }
}
