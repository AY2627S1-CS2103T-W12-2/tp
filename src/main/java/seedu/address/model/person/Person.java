package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final LinkedIn linkedIn;
    private final Set<Tag> tags = new HashSet<>();
    private final Note note;

    // Activity metadata (when the person was created, last edited and last viewed)
    private final Activity activity;
    private final Meeting meeting;

    /**
     * Creates a new person with no known activity times.
     * Use this for a contact that does not exist yet, and in tests. To change an existing contact, copy it with
     * {@link #withActivity(Activity)} or the model's edit operation so its activity times are kept.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags) {
        this(name, phone, email, linkedIn, tags, Activity.EMPTY);
    }

    /**
     * Creates a person with the given activity times. Every field must be present and not null.
     * To change an existing contact, copy it with {@link #withActivity(Activity)} or the model's edit operation.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags, Activity activity) {
        this(name, phone, email, linkedIn, tags, activity, Note.EMPTY);
    }

    /**
     * Creates a person with the given activity times and note. Every field must be present and not null.
     * To change an existing contact, copy it with {@link #toBuilder()} or the model's edit operation.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags, Activity activity,
            Note note) {
        this(name, phone, email, linkedIn, tags, activity, note, Meeting.EMPTY);
    }

    /** Creates a complete contact with optional meeting context. */
    private Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags, Activity activity,
            Note note, Meeting meeting) {
        requireAllNonNull(name, phone, email, linkedIn, tags, activity, note, meeting);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.linkedIn = linkedIn;
        this.tags.addAll(tags);
        this.activity = activity;
        this.note = note;
        this.meeting = meeting;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public LinkedIn getLinkedIn() {
        return linkedIn;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public Note getNote() {
        return note;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public Activity getActivity() {
        return activity;
    }

    /**
     * Returns a copy of this person with every field kept except {@code activity}, which is replaced.
     */
    public Person withActivity(Activity activity) {
        return toBuilder().withActivity(activity).build();
    }

    /**
     * Returns a builder that starts with every field of this person. Use it to copy a contact with some fields
     * changed, so that no field (including ones added by later features) is lost by an incomplete constructor call.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns true if both persons have the same identity and data fields, ignoring {@code activity}.
     * Used to tell a genuine edit from a no-op.
     */
    public boolean hasSameDetails(Person otherPerson) {
        return otherPerson != null
                && withActivity(Activity.EMPTY).equals(otherPerson.withActivity(Activity.EMPTY));
    }

    /**
     * Returns true if both persons have the same name, ignoring case, and the same phone number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().fullName.equalsIgnoreCase(getName().fullName)
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && linkedIn.equals(otherPerson.linkedIn)
                && tags.equals(otherPerson.tags)
                && note.equals(otherPerson.note)
                && activity.equals(otherPerson.activity)
                && meeting.equals(otherPerson.meeting);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, linkedIn, tags, note, activity, meeting);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("linkedIn", linkedIn)
                .add("tags", tags)
                // the text of a note is personal and toString() is used for logging, so only its length is shown
                .add("note", note.toLogString())
                .add("activity", activity)
                .add("meeting", meeting)
                .toString();
    }

    /**
     * Builds a {@code Person} by copying another one and changing some fields.
     * Obtained with {@link Person#toBuilder()}. Every field of the source person is carried over, so a field that
     * is not set here keeps its old value. A new {@code Person} field must be added to this class too.
     */
    public static class Builder {
        private Name name;
        private Phone phone;
        private Email email;
        private LinkedIn linkedIn;
        private Set<Tag> tags;
        private Note note;
        private Activity activity;
        private Meeting meeting;

        private Builder(Person source) {
            name = source.name;
            phone = source.phone;
            email = source.email;
            linkedIn = source.linkedIn;
            tags = new HashSet<>(source.tags);
            note = source.note;
            activity = source.activity;
            meeting = source.meeting;
        }

        /**
         * Replaces the name.
         */
        public Builder withName(Name name) {
            requireNonNull(name);
            this.name = name;
            return this;
        }

        /**
         * Replaces the phone.
         */
        public Builder withPhone(Phone phone) {
            requireNonNull(phone);
            this.phone = phone;
            return this;
        }

        /**
         * Replaces the email.
         */
        public Builder withEmail(Email email) {
            requireNonNull(email);
            this.email = email;
            return this;
        }

        /**
         * Replaces the LinkedIn handle.
         */
        public Builder withLinkedIn(LinkedIn linkedIn) {
            requireNonNull(linkedIn);
            this.linkedIn = linkedIn;
            return this;
        }

        /**
         * Replaces the tags. A copy of {@code tags} is kept.
         */
        public Builder withTags(Set<Tag> tags) {
            requireNonNull(tags);
            this.tags = new HashSet<>(tags);
            return this;
        }

        /**
         * Replaces the note. An empty note removes the note.
         */
        public Builder withNote(Note note) {
            requireNonNull(note);
            this.note = note;
            return this;
        }

        /**
         * Replaces the activity times.
         */
        public Builder withActivity(Activity activity) {
            requireNonNull(activity);
            this.activity = activity;
            return this;
        }

        /** Replaces optional meeting context without losing other fields. */
        public Builder withMeeting(Meeting meeting) {
            this.meeting = requireNonNull(meeting);
            return this;
        }

        /**
         * Returns a new {@code Person} with the fields set so far.
         */
        public Person build() {
            return new Person(name, phone, email, linkedIn, tags, activity, note, meeting);
        }
    }

}
