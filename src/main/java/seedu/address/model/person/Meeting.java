package seedu.address.model.person;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/** Immutable, optional context about where and when a contact was met. */
public final class Meeting {
    public static final Meeting EMPTY = new Meeting(null, null, null);
    public static final String MESSAGE_TEXT = "Event and role must each contain 1 to 80 characters.";
    public static final String MESSAGE_DATE = "Date must be a real date in YYYY-MM-DD format.";

    private final String eventName;
    private final LocalDate date;
    private final String role;

    /** Creates meeting context. Null fields represent absent values; present text must be nonblank. */
    public Meeting(String eventName, LocalDate date, String role) {
        validateText(eventName);
        validateText(role);
        if (date != null && (date.getYear() < 1 || date.getYear() > 9999)) {
            throw new IllegalArgumentException(MESSAGE_DATE);
        }
        this.eventName = eventName;
        this.date = date;
        this.role = role;
    }

    /** Rejects blank or overlong present text without inventing a value for absent fields. */
    private static void validateText(String value) {
        if (value != null && (value.isBlank() || value.length() > 80)) {
            throw new IllegalArgumentException(MESSAGE_TEXT);
        }
    }

    /** Parses a real calendar date in strict four-digit-year ISO format. */
    public static LocalDate parseDate(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(MESSAGE_DATE);
        }
        try {
            LocalDate parsed = LocalDate.parse(value);
            if (parsed.getYear() > 0) {
                return parsed;
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_DATE, e);
        }
        throw new IllegalArgumentException(MESSAGE_DATE);
    }

    public Optional<String> getEventName() {
        return Optional.ofNullable(eventName);
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<String> getRole() {
        return Optional.ofNullable(role);
    }

    public boolean isEmpty() {
        return eventName == null && date == null && role == null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Meeting meeting && Objects.equals(eventName, meeting.eventName)
                && Objects.equals(date, meeting.date) && Objects.equals(role, meeting.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, date, role);
    }

    /** Returns a presence-only summary that does not expose personal meeting context in logs. */
    public String toLogString() {
        return isEmpty() ? "none" : "[hidden]";
    }

    @Override
    public String toString() {
        return "Meeting[event=" + eventName + ", date=" + date + ", role=" + role + "]";
    }
}
