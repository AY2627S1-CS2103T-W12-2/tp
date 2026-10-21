package seedu.address.storage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Meeting;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    /** Message for a malformed field in the data file, as {@code Invalid contact data: '<field>' <reason>.} */
    public static final String INVALID_FIELD_MESSAGE_FORMAT = "Invalid contact data: '%s' %s.";
    public static final String REASON_INVALID_TIME = "must be an ISO-8601 UTC timestamp";
    public static final String REASON_INVALID_NOTE =
            "must be a string of at most " + Note.MAX_LENGTH + " characters";

    /** A whole-second UTC time such as {@code 2026-10-17T04:12:33Z}. */
    private static final Pattern WHOLE_SECOND_UTC =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");

    private final String name;
    private final String phone;
    private final String email;
    private final String linkedIn;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    // Object, not String, so that a non-string value reaches parseTime and gets the field-specific message
    private final Object createdAt;
    private final Object lastEditedAt;
    private final Object lastViewedAt;
    // Object, not String, so that a non-string value reaches parseNote and gets the field-specific message
    private final Object note;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Object meeting;
    private Object photoPath;
    private Object followUpDate;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details, no activity times and no note.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String linkedIn,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, linkedIn, tags, null, null, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details and no note.
     * The activity times are ISO-8601 UTC strings, or null if not known.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String linkedIn,
            List<JsonAdaptedTag> tags, Object createdAt, Object lastEditedAt, Object lastViewedAt) {
        this(name, phone, email, linkedIn, tags, createdAt, lastEditedAt, lastViewedAt, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * The activity times are ISO-8601 UTC strings, or null if not known. The note is a string, or null if the
     * person has no note.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("linkedIn") String linkedIn,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("createdAt") Object createdAt,
            @JsonProperty("lastEditedAt") Object lastEditedAt, @JsonProperty("lastViewedAt") Object lastViewedAt,
            @JsonProperty("note") Object note) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.linkedIn = linkedIn;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.createdAt = createdAt;
        this.lastEditedAt = lastEditedAt;
        this.lastViewedAt = lastViewedAt;
        this.note = note;
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        linkedIn = source.getLinkedIn().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        if (!source.getMeeting().isEmpty()) {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            source.getMeeting().getEventName().ifPresent(value -> values.put("eventName", value));
            source.getMeeting().getDate().ifPresent(value -> values.put("date", value.toString()));
            source.getMeeting().getRole().ifPresent(value -> values.put("role", value));
            meeting = values;
        }
        photoPath = source.getPhotoPath().isEmpty() ? null : source.getPhotoPath();
        followUpDate = source.getFollowUpDate().map(LocalDate::toString).orElse(null);
        Activity activity = source.getActivity();
        createdAt = activity.getCreatedAt().map(Instant::toString).orElse(null);
        lastEditedAt = activity.getLastEditedAt().map(Instant::toString).orElse(null);
        lastViewedAt = activity.getLastViewedAt().map(Instant::toString).orElse(null);
        note = source.getNote().isEmpty() ? null : source.getNote().value;
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone != null && !Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = phone == null ? new Phone("") : new Phone(phone);

        if (email != null && !Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = email == null ? new Email("") : new Email(email);

        // A missing LinkedIn field is treated as no handle, so that older data files still load.
        if (linkedIn != null && !LinkedIn.isValidLinkedIn(linkedIn)) {
            throw new IllegalValueException(LinkedIn.MESSAGE_CONSTRAINTS);
        }
        final LinkedIn modelLinkedIn = new LinkedIn(linkedIn == null ? "" : linkedIn);

        final Set<Tag> modelTags = new HashSet<>(personTags);

        // Missing activity times (for example, in older data files) stay absent instead of being invented.
        final Activity modelActivity = new Activity(parseTime("createdAt", createdAt),
                parseTime("lastEditedAt", lastEditedAt), parseTime("lastViewedAt", lastViewedAt));
        final Note modelNote = parseNote(note);
        return new Person(modelName, modelPhone, modelEmail, modelLinkedIn, modelTags, modelActivity, modelNote)
                .toBuilder().withMeeting(parseMeeting()).withPhotoPath(readPhotoReference())
                .withFollowUpDate(parseFollowUpDate()).build();
    }

    /**
     * Parses a note from the data file. Returns an empty note if {@code value} is null (the person has no note, or
     * the file is from before notes existed). A note that is not a string, is empty, is blank, or is longer than
     * {@value Note#MAX_LENGTH} characters is rejected, because a person without a note is saved as null.
     *
     * @throws IllegalValueException if {@code value} is not a valid note.
     */
    private static Note parseNote(Object value) throws IllegalValueException {
        if (value == null) {
            return Note.EMPTY;
        }
        if (!(value instanceof String text) || text.isEmpty() || !Note.isValidNote(text)) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "note", REASON_INVALID_NOTE));
        }
        return new Note(text);
    }

    /** Reads the optional follow-up date; a missing or null value means no follow-up. */
    private LocalDate parseFollowUpDate() throws IllegalValueException {
        if (followUpDate == null) {
            return null;
        }
        try {
            return Meeting.parseDate(followUpDate instanceof String text ? text : "");
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "followUpDate",
                    "must be a real date in YYYY-MM-DD format"));
        }
    }

    /** Reads optional context without coercing malformed JSON into valid strings. */
    private Meeting parseMeeting() throws IllegalValueException {
        if (meeting == null) {
            return Meeting.EMPTY;
        }
        if (!(meeting instanceof Map<?, ?> values)) {
            throw new IllegalValueException(
                    String.format(INVALID_FIELD_MESSAGE_FORMAT, "meeting", "must be an object"));
        }
        String event = meetingText(values, "eventName");
        String role = meetingText(values, "role");
        Object date = values.get("date");
        try {
            return new Meeting(event, date == null ? null : Meeting.parseDate(date instanceof String text ? text : ""),
                    role);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "meeting.date",
                    "must be a real date in YYYY-MM-DD format"));
        }
    }

    /** Validates each present event/role value and names the offending JSON field. */
    private String meetingText(Map<?, ?> values, String field) throws IllegalValueException {
        Object value = values.get(field);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text) || text.isBlank() || text.length() > 80) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "meeting." + field,
                    "must contain 1 to 80 nonblank characters"));
        }
        return text;
    }

    /** Keeps malformed photo references from blocking otherwise usable contact data. */
    private String readPhotoReference() {
        if (photoPath == null) {
            return "";
        }
        if (photoPath instanceof String reference) {
            return reference;
        }
        seedu.address.commons.core.LogsCenter.getLogger(JsonAdaptedPerson.class)
                .warning("Invalid photoPath; showing a photo placeholder.");
        return "";
    }

    /**
     * Parses a whole-second ISO-8601 UTC timestamp such as {@code 2026-10-17T04:12:33Z}.
     * Returns null if {@code time} is null. Fractions of a second, offsets other than {@code Z},
     * surrounding spaces, empty text and values that are not JSON strings are rejected.
     *
     * @throws IllegalValueException if {@code time} is not such a timestamp, naming {@code field}.
     */
    private static Instant parseTime(String field, Object value) throws IllegalValueException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            // a JSON object, array, number or boolean
            throw new IllegalValueException(
                    String.format(INVALID_FIELD_MESSAGE_FORMAT, field, REASON_INVALID_TIME));
        }
        String time = (String) value;
        try {
            if (WHOLE_SECOND_UTC.matcher(time).matches()) {
                Instant parsed = Instant.parse(time);
                // Instant.parse also accepts 24:00:00 and leap seconds, which it moves to another time
                if (parsed.toString().equals(time)) {
                    return parsed;
                }
            }
        } catch (DateTimeParseException e) {
            // fall through: the text looks like a time but is not a real one, such as 2026-02-30
        }
        throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, field, REASON_INVALID_TIME));
    }

}
