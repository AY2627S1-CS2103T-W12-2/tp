package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_sixContactsWithLinkedInAndTags() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();

        assertEquals(6, samplePersons.length);
        for (Person person : samplePersons) {
            assertFalse(person.getLinkedIn().isEmpty());
            assertFalse(person.getTags().isEmpty());
        }
    }
}
