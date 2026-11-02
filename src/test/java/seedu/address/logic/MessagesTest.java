package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_personWithLinkedIn_includesLinkedIn() {
        Person person = new PersonBuilder().withLinkedIn(VALID_LINKEDIN_AMY).withTags("friends").build();
        String expected = person.getName() + "; Phone: " + person.getPhone() + "; Email: " + person.getEmail()
                + "; LinkedIn: " + VALID_LINKEDIN_AMY + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void format_personWithoutLinkedIn_omitsLinkedIn() {
        Person person = new PersonBuilder().withLinkedIn("").withTags("friends").build();
        String expected = person.getName() + "; Phone: " + person.getPhone() + "; Email: " + person.getEmail()
                + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void format_personWithOnlyName_omitsEmptyFields() {
        Person person = new PersonBuilder().withPhone("").withEmail("").withLinkedIn("").withTags().build();
        assertEquals(person.getName().toString(), Messages.format(person));
    }

    @Test
    public void format_personWithoutTags_omitsTags() {
        Person person = new PersonBuilder().withLinkedIn("").withTags().build();
        String expected = person.getName() + "; Phone: " + person.getPhone() + "; Email: " + person.getEmail();
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void format_personWithoutPhone_omitsPhone() {
        Person person = new PersonBuilder().withPhone("").withLinkedIn("").withTags("friends").build();
        String expected = person.getName() + "; Email: " + person.getEmail() + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void format_personWithoutEmail_omitsEmail() {
        Person person = new PersonBuilder().withEmail("").withLinkedIn("").withTags("friends").build();
        String expected = person.getName() + "; Phone: " + person.getPhone() + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }
}
