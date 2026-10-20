package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class FilterCommandTest {

    private Model modelWithAliceStarred() {
        AddressBook addressBook = getTypicalAddressBook();
        addressBook.setPerson(ALICE, ALICE.toBuilder().withStarred(true).build());
        return new ModelManager(addressBook, new UserPrefs(), FIXED_CLOCK);
    }

    @Test
    public void execute_starredOn_showsOnlyStarredPersons() {
        Model model = modelWithAliceStarred();

        CommandResult result = new FilterCommand(true).execute(model);

        assertEquals(String.format(FilterCommand.MESSAGE_STARRED_ON, 1), result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().get(0).isStarred());
    }

    @Test
    public void execute_noStarredPersons_showsEmptyList() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

        CommandResult result = new FilterCommand(true).execute(model);

        assertEquals(String.format(FilterCommand.MESSAGE_STARRED_ON, 0), result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_starredOff_showsAllPersonsAgain() {
        Model model = modelWithAliceStarred();
        new FilterCommand(true).execute(model);

        CommandResult result = new FilterCommand(false).execute(model);

        List<Person> all = model.getAddressBook().getPersonList();
        assertEquals(String.format(FilterCommand.MESSAGE_STARRED_OFF, all.size()), result.getFeedbackToUser());
        assertEquals(all, model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        FilterCommand on = new FilterCommand(true);

        assertEquals(on, on);
        assertEquals(on, new FilterCommand(true));
        assertNotEquals(on, new FilterCommand(false));
        assertNotEquals(on, null);
        assertNotEquals(on, new ClearCommand());
    }

    @Test
    public void toStringMethod() {
        assertEquals(FilterCommand.class.getCanonicalName() + "{starredOnly=true}",
                new FilterCommand(true).toString());
    }
}
