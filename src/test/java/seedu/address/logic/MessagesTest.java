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
                + "; Address: " + person.getAddress() + "; LinkedIn: " + VALID_LINKEDIN_AMY + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void format_personWithoutLinkedIn_omitsLinkedIn() {
        Person person = new PersonBuilder().withLinkedIn("").withTags("friends").build();
        String expected = person.getName() + "; Phone: " + person.getPhone() + "; Email: " + person.getEmail()
                + "; Address: " + person.getAddress() + "; Tags: [friends]";
        assertEquals(expected, Messages.format(person));
    }
}
