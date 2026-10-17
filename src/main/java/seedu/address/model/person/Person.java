package seedu.address.model.person;

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
    private final FollowUpDate followUpDate;

    // Activity metadata (when the person was created, last edited and last viewed)
    private final Activity activity;

    /**
     * Creates a person with no known activity times.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags) {
        this(name, phone, email, linkedIn, tags, Activity.EMPTY);
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags, Activity activity) {
        this(name, phone, email, linkedIn, tags, FollowUpDate.NONE, activity);
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, LinkedIn linkedIn, Set<Tag> tags,
            FollowUpDate followUpDate, Activity activity) {
        requireAllNonNull(name, phone, email, linkedIn, tags, followUpDate, activity);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.linkedIn = linkedIn;
        this.tags.addAll(tags);
        this.followUpDate = followUpDate;
        this.activity = activity;
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

    public FollowUpDate getFollowUpDate() {
        return followUpDate;
    }

    public Activity getActivity() {
        return activity;
    }

    /**
     * Returns a copy of this person with every field kept except {@code activity}, which is replaced.
     */
    public Person withActivity(Activity activity) {
        return new Person(name, phone, email, linkedIn, tags, followUpDate, activity);
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
                && followUpDate.equals(otherPerson.followUpDate)
                && activity.equals(otherPerson.activity);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, linkedIn, tags, followUpDate, activity);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("linkedIn", linkedIn)
                .add("tags", tags)
                .add("followUpDate", followUpDate)
                .add("activity", activity)
                .toString();
    }

}
