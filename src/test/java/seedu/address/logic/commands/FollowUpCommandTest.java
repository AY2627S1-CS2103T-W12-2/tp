package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.EARLIER;
import static seedu.address.testutil.TypicalTimes.EARLIEST;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.FollowUpDate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class FollowUpCommandTest {

    private static final FollowUpDate DATE = new FollowUpDate("2026-10-25");
    private static final FollowUpDate OTHER_DATE = new FollowUpDate("2026-11-02");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

    private Model copyOfModel() {
        return new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), FIXED_CLOCK);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpCommand(null, DATE));
        assertThrows(NullPointerException.class, () -> new FollowUpCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_setDate_success() {
        Person updated = new PersonBuilder(ALICE).withFollowUpDate(DATE.value).build();
        Model expectedModel = copyOfModel();
        expectedModel.editPerson(ALICE, updated);

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, DATE), model,
                String.format(FollowUpCommand.MESSAGE_SET_SUCCESS, Messages.format(updated)), expectedModel);
        assertEquals(DATE, model.getFilteredPersonList().get(0).getFollowUpDate());
    }

    @Test
    public void execute_setDate_keepsOtherFields() throws Exception {
        new FollowUpCommand(INDEX_SECOND_PERSON, DATE).execute(model);

        Person updated = model.getFilteredPersonList().get(1);
        assertTrue(updated.hasSameDetails(new PersonBuilder(BENSON).withFollowUpDate(DATE.value).build()));
        assertEquals(BENSON.getTags(), updated.getTags());
        assertEquals(BENSON.getLinkedIn(), updated.getLinkedIn());
    }

    @Test
    public void execute_replaceDate_success() throws Exception {
        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);
        Person withDate = model.getFilteredPersonList().get(0);
        Person replaced = new PersonBuilder(withDate).withFollowUpDate(OTHER_DATE.value).build();
        Model expectedModel = copyOfModel();
        expectedModel.editPerson(withDate, replaced);

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, OTHER_DATE), model,
                String.format(FollowUpCommand.MESSAGE_SET_SUCCESS, Messages.format(replaced)), expectedModel);
        assertEquals(OTHER_DATE, model.getFilteredPersonList().get(0).getFollowUpDate());
    }

    @Test
    public void execute_clearDate_success() throws Exception {
        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);
        Person withDate = model.getFilteredPersonList().get(0);
        Person cleared = new PersonBuilder(withDate).withFollowUpDate("").build();
        Model expectedModel = copyOfModel();
        expectedModel.editPerson(withDate, cleared);

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE), model,
                String.format(FollowUpCommand.MESSAGE_CLEAR_SUCCESS, Messages.format(cleared)), expectedModel);
        assertTrue(model.getFilteredPersonList().get(0).getFollowUpDate().isEmpty());
    }

    @Test
    public void execute_clearDateAlreadyEmpty_isNoOp() {
        Model expectedModel = copyOfModel();

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE), model,
                String.format(FollowUpCommand.MESSAGE_NOTHING_TO_CLEAR, Messages.format(ALICE)), expectedModel);
        assertEquals(Activity.EMPTY, model.getFilteredPersonList().get(0).getActivity());
    }

    @Test
    public void execute_sameDateAgain_doesNotChangeActivity() throws Exception {
        Person withDate = new PersonBuilder(ALICE).withFollowUpDate(DATE.value)
                .withActivity(new Activity(EARLIEST, EARLIER, EARLIER)).build();
        model.setPerson(ALICE, withDate);

        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);

        assertEquals(withDate, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_changedDate_setsLastEditedAndKeepsOtherTimes() throws Exception {
        model.setPerson(ALICE, ALICE.withActivity(new Activity(EARLIEST, EARLIER, EARLIER)));

        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);

        assertEquals(new Activity(EARLIEST, NOW, EARLIER), model.getFilteredPersonList().get(0).getActivity());
    }

    @Test
    public void execute_clearedDate_setsLastEdited() throws Exception {
        Person withDate = new PersonBuilder(ALICE).withFollowUpDate(DATE.value)
                .withActivity(new Activity(EARLIEST, EARLIER, null)).build();
        model.setPerson(ALICE, withDate);

        new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE).execute(model);

        assertEquals(new Activity(EARLIEST, NOW, null), model.getFilteredPersonList().get(0).getActivity());
    }

    @Test
    public void execute_validIndexFilteredList_updatesDisplayedPerson() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);

        // the displayed person (second in the address book) got the date, not the first
        assertEquals(DATE, model.getFilteredPersonList().get(0).getFollowUpDate());
        assertEquals(BENSON.getName(), model.getFilteredPersonList().get(0).getName());
        assertTrue(model.getAddressBook().getPersonList().get(0).getFollowUpDate().isEmpty());
    }

    @Test
    public void execute_viewedPerson_detailShowsNewDate() throws Exception {
        model.viewPerson(model.getFilteredPersonList().get(0));

        new FollowUpCommand(INDEX_FIRST_PERSON, DATE).execute(model);

        assertEquals(DATE, model.getViewedPerson().get().getFollowUpDate());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new FollowUpCommand(outOfBoundIndex, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertTrue(model.getAddressBook().getPersonList().stream().allMatch(p -> p.getFollowUpDate().isEmpty()));
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new FollowUpCommand(INDEX_SECOND_PERSON, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_failure() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);

        assertCommandFailure(new FollowUpCommand(INDEX_FIRST_PERSON, DATE), emptyModel,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        FollowUpCommand command = new FollowUpCommand(INDEX_FIRST_PERSON, DATE);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new FollowUpCommand(INDEX_FIRST_PERSON, new FollowUpDate(DATE.value))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(5));
        assertFalse(command.equals(new FollowUpCommand(INDEX_SECOND_PERSON, DATE)));
        assertFalse(command.equals(new FollowUpCommand(INDEX_FIRST_PERSON, OTHER_DATE)));
        assertFalse(command.equals(new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE)));
    }

    @Test
    public void toStringMethod() {
        FollowUpCommand command = new FollowUpCommand(INDEX_FIRST_PERSON, DATE);
        String expected = FollowUpCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", followUpDate=" + DATE + "}";
        assertEquals(expected, command.toString());
    }
}
