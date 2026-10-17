package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.EARLIER;
import static seedu.address.testutil.TypicalTimes.EARLIEST;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code ViewCommand}.
 */
public class ViewCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToView = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(ViewCommand.MESSAGE_VIEW_PERSON_SUCCESS,
                Messages.format(personToView));

        // viewing a person only records the last viewed time
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
        expectedModel.viewPerson(personToView);

        assertCommandSuccess(viewCommand, model, expectedMessage, expectedModel);
        assertTrue(personToView.hasSameDetails(model.getViewedPerson().get()));
        assertEquals(new Activity(null, null, NOW), model.getViewedPerson().get().getActivity());
    }

    @Test
    public void execute_viewAnotherPerson_replacesViewedPerson() throws Exception {
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        new ViewCommand(INDEX_SECOND_PERSON).execute(model);

        assertTrue(secondPerson.hasSameDetails(model.getViewedPerson().get()));
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertNull(model.getViewedPerson().get());
    }

    @Test
    public void execute_invalidIndexAfterViewingPerson_keepsViewedPerson() throws Exception {
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        Person viewedPerson = model.getViewedPerson().get();
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        assertEquals(viewedPerson, model.getViewedPerson().get());
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertTrue(model.getFilteredPersonList().isEmpty());

        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_EMPTY_PERSON_LIST);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToView = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(ViewCommand.MESSAGE_VIEW_PERSON_SUCCESS,
                Messages.format(personToView));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        expectedModel.viewPerson(personToView);

        assertCommandSuccess(viewCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(viewFirstCommand.equals(viewFirstCommand));

        // same values -> returns true
        ViewCommand viewFirstCommandCopy = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(viewFirstCommand.equals(viewFirstCommandCopy));

        // different types -> returns false
        assertFalse(viewFirstCommand.equals(1));

        // null -> returns false
        assertFalse(viewFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(viewFirstCommand.equals(viewSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        ViewCommand viewCommand = new ViewCommand(targetIndex);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, viewCommand.toString());
    }

    @Test
    public void execute_viewedPerson_keepsCreatedAndLastEdited() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, original.withActivity(new Activity(EARLIEST, EARLIER, null)));

        new ViewCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(new Activity(EARLIEST, EARLIER, NOW), model.getFilteredPersonList().get(0).getActivity());
        assertEquals(model.getFilteredPersonList().get(0), model.getViewedPerson().get());
    }

    @Test
    public void execute_failedView_doesNotChangeActivity() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        assertEquals(Activity.EMPTY, model.getFilteredPersonList().get(0).getActivity());
    }
}
