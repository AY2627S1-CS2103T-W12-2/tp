package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    /**
     * Returns six fictional hackathon contacts with LinkedIn handles and stack or role tags.
     */
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Aisha Rahman"), new Phone("80000001"), new Email("aisha@example.com"),
                    new LinkedIn("aisha-rahman"), getTagSet("react", "frontend")),
            new Person(new Name("Ben Tan"), new Phone("80000002"), new Email("ben@example.com"),
                    new LinkedIn("ben-tan"), getTagSet("python", "backend")),
            new Person(new Name("Chloe Lim"), new Phone("80000003"), new Email("chloe@example.com"),
                    new LinkedIn("chloe-lim"), getTagSet("figma", "designer")),
            new Person(new Name("Daniel Koh"), new Phone("80000004"), new Email("daniel@example.com"),
                    new LinkedIn("daniel-koh"), getTagSet("flutter", "mobile")),
            new Person(new Name("Ethan Wong"), new Phone("80000005"), new Email("ethan@example.com"),
                    new LinkedIn("ethan-wong"), getTagSet("nodejs", "devops")),
            new Person(new Name("Farah Ali"), new Phone("80000006"), new Email("farah@example.com"),
                    new LinkedIn("farah-ali"), getTagSet("java", "fullstack"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
