package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;

/**
 * Represents the date on which to follow up with a person.
 * The date is optional: an empty value means there is no follow-up date.
 * Guarantees: immutable; is valid as declared in {@link #isValidFollowUpDate(String)}
 */
public class FollowUpDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Follow-up dates must be real calendar dates in the format YYYY-MM-DD, for example 2026-10-25.";

    /** A follow-up date that is not set. The empty value never uses the private formatter below. */
    public static final FollowUpDate NONE = new FollowUpDate("");

    /** Exactly four digit year, two digit month and two digit day. */
    private static final String FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    /** The date written as {@code YYYY-MM-DD}. Strict, so that dates like 2026-02-30 are rejected. */
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** The date as {@code YYYY-MM-DD}, or an empty string if there is no date. */
    public final String value;

    private final LocalDate date;

    /**
     * Constructs a {@code FollowUpDate}.
     *
     * @param followUpDate A date as {@code YYYY-MM-DD}, or an empty string for no date.
     */
    public FollowUpDate(String followUpDate) {
        requireNonNull(followUpDate);
        checkArgument(isValidFollowUpDate(followUpDate), MESSAGE_CONSTRAINTS);
        value = followUpDate;
        date = followUpDate.isEmpty() ? null : LocalDate.parse(followUpDate, FORMAT);
    }

    /**
     * Returns true if a given string is an empty string (no date) or a real calendar date as {@code YYYY-MM-DD}.
     */
    public static boolean isValidFollowUpDate(String test) {
        requireNonNull(test);
        if (test.isEmpty()) {
            return true;
        }
        if (!test.matches(FORMAT_REGEX)) {
            return false;
        }
        try {
            LocalDate.parse(test, FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean isEmpty() {
        return date == null;
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    /**
     * Returns true if there is a date and it is {@code today} or earlier.
     */
    public boolean isDueOn(LocalDate today) {
        requireNonNull(today);
        return date != null && !date.isAfter(today);
    }

    /**
     * Returns true if there is a date and it is before {@code today}. A date that is today is due, not overdue.
     */
    public boolean isOverdueOn(LocalDate today) {
        requireNonNull(today);
        return date != null && date.isBefore(today);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FollowUpDate otherFollowUpDate)) {
            return false;
        }

        return value.equals(otherFollowUpDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
