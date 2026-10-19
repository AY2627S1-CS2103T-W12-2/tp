package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;

import seedu.address.model.person.Activity;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_LINKEDIN = "";

    private Name name;
    private Phone phone;
    private Email email;
    private LinkedIn linkedIn;
    private Set<Tag> tags;
    private Activity activity;
    private String photoPath;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        linkedIn = new LinkedIn(DEFAULT_LINKEDIN);
        tags = new HashSet<>();
        activity = Activity.EMPTY;
        photoPath = "";
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        linkedIn = personToCopy.getLinkedIn();
        tags = new HashSet<>(personToCopy.getTags());
        activity = personToCopy.getActivity();
        photoPath = personToCopy.getPhotoPath();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code LinkedIn} of the {@code Person} that we are building.
     */
    public PersonBuilder withLinkedIn(String linkedIn) {
        this.linkedIn = new LinkedIn(linkedIn);
        return this;
    }

    /**
     * Sets the {@code Activity} of the {@code Person} that we are building.
     */
    public PersonBuilder withActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    /** Builds a person with every copied field retained. */
    public Person build() {
        return new Person(name, phone, email, linkedIn, tags, activity)
                .toBuilder().withPhotoPath(photoPath).build();
    }

}
