package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
        expectedModel.addPerson(validPerson.withActivity(new Activity(NOW, null, null)));

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_newPerson_recordsCreatedTimeOnly() throws Exception {
        new AddCommand(new PersonBuilder().build()).execute(model);

        Person added = model.getAddressBook().getPersonList().get(model.getAddressBook().getPersonList().size() - 1);
        assertEquals(new Activity(NOW, null, null), added.getActivity());
    }

    @Test
    public void execute_duplicatePerson_doesNotChangeAddressBook() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);

        assertCommandFailure(new AddCommand(personInList), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertEquals(expectedModel, model);
    }

}
