package seedu.address.storage;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUpDate;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_INVALID_TIME_FORMAT =
            "Activity times must be ISO-8601 UTC timestamps, for example 2026-10-17T04:12:33Z.";

    private final String name;
    private final String phone;
    private final String email;
    private final String linkedIn;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final String followUpDate;
    private final String createdAt;
    private final String lastEditedAt;
    private final String lastViewedAt;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details and no activity times.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String linkedIn,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, linkedIn, tags, null, null, null, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * The follow-up date is {@code YYYY-MM-DD}, or null if not set.
     * The activity times are ISO-8601 UTC strings, or null if not known.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("linkedIn") String linkedIn,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("createdAt") String createdAt,
            @JsonProperty("lastEditedAt") String lastEditedAt, @JsonProperty("lastViewedAt") String lastViewedAt,
            @JsonProperty("followUpDate") String followUpDate) {
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
        this.followUpDate = followUpDate;
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
        followUpDate = source.getFollowUpDate().value;
        Activity activity = source.getActivity();
        createdAt = activity.getCreatedAt().map(Instant::toString).orElse(null);
        lastEditedAt = activity.getLastEditedAt().map(Instant::toString).orElse(null);
        lastViewedAt = activity.getLastViewedAt().map(Instant::toString).orElse(null);
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

        // A missing follow-up date is treated as no date, so that older data files still load.
        if (followUpDate != null && !FollowUpDate.isValidFollowUpDate(followUpDate)) {
            throw new IllegalValueException(FollowUpDate.MESSAGE_CONSTRAINTS);
        }
        final FollowUpDate modelFollowUpDate = new FollowUpDate(followUpDate == null ? "" : followUpDate);

        // Missing activity times (for example, in older data files) stay absent instead of being invented.
        final Activity modelActivity = new Activity(parseTime(createdAt), parseTime(lastEditedAt),
                parseTime(lastViewedAt));
        return new Person(modelName, modelPhone, modelEmail, modelLinkedIn, modelTags, modelFollowUpDate,
                modelActivity);
    }

    /**
     * Parses an ISO-8601 UTC timestamp such as {@code 2026-10-17T04:12:33Z}. Returns null if {@code time} is null.
     *
     * @throws IllegalValueException if {@code time} is not an ISO-8601 UTC timestamp.
     */
    private static Instant parseTime(String time) throws IllegalValueException {
        if (time == null) {
            return null;
        }
        try {
            return Instant.parse(time);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(MESSAGE_INVALID_TIME_FORMAT);
        }
    }

}
