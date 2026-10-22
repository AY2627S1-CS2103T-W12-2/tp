package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class FollowUpCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 25);
    private static final LocalDate OTHER_DATE = LocalDate.of(2026, 11, 1);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

    @Test
    public void execute_setDate_success() {
        Model expectedModel = copyOf(model);
        expectedModel.editPerson(ALICE, ALICE.toBuilder().withFollowUpDate(DATE).build());

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, DATE), model,
                String.format(FollowUpCommand.MESSAGE_SET_SUCCESS, ALICE.getName().fullName, DATE), expectedModel);
        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(Optional.of(DATE), updated.getFollowUpDate());
        assertTrue(updated.getActivity().getLastEditedAt().isPresent());
    }

    @Test
    public void execute_replaceDate_success() {
        Person withDate = ALICE.toBuilder().withFollowUpDate(DATE).build();
        Model modelWithDate = modelWith(ALICE, withDate);
        Model expectedModel = copyOf(modelWithDate);
        expectedModel.editPerson(withDate, withDate.toBuilder().withFollowUpDate(OTHER_DATE).build());

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, OTHER_DATE), modelWithDate,
                String.format(FollowUpCommand.MESSAGE_SET_SUCCESS, ALICE.getName().fullName, OTHER_DATE),
                expectedModel);
    }

    @Test
    public void execute_clearDate_success() {
        Person withDate = ALICE.toBuilder().withFollowUpDate(DATE).build();
        Model modelWithDate = modelWith(ALICE, withDate);
        Model expectedModel = copyOf(modelWithDate);
        expectedModel.editPerson(withDate, withDate.toBuilder().withFollowUpDate(null).build());

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, null), modelWithDate,
                String.format(FollowUpCommand.MESSAGE_CLEARED, ALICE.getName().fullName), expectedModel);
        assertEquals(Optional.empty(), modelWithDate.getFilteredPersonList().get(0).getFollowUpDate());
    }

    @Test
    public void execute_sameDate_isNoOpAndDoesNotTouchLastEdited() {
        Person withDate = ALICE.toBuilder().withFollowUpDate(DATE).build();
        Model modelWithDate = modelWith(ALICE, withDate);
        Model expectedModel = copyOf(modelWithDate);

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, DATE), modelWithDate,
                FollowUpCommand.MESSAGE_UNCHANGED, expectedModel);
        assertEquals(withDate.getActivity(), modelWithDate.getFilteredPersonList().get(0).getActivity());
        assertFalse(modelWithDate.canUndo());
    }

    @Test
    public void execute_clearAbsentDate_isNoOp() {
        Model expectedModel = copyOf(model);

        assertCommandSuccess(new FollowUpCommand(INDEX_FIRST_PERSON, null), model,
                FollowUpCommand.MESSAGE_UNCHANGED, expectedModel);
        assertEquals(ALICE.getActivity(), model.getFilteredPersonList().get(0).getActivity());
    }

    @Test
    public void execute_outOfBoundsIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new FollowUpCommand(outOfBounds, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new FollowUpCommand(outOfBounds, null), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_otherFieldsKept() {
        Person target = model.getFilteredPersonList().get(0);

        new FollowUpCommandRunner().run(model, new FollowUpCommand(INDEX_FIRST_PERSON, DATE));

        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(target.getName(), updated.getName());
        assertEquals(target.getTags(), updated.getTags());
        assertEquals(target.getNote(), updated.getNote());
        assertEquals(target.getMeeting(), updated.getMeeting());
        assertEquals(target.getPhotoPath(), updated.getPhotoPath());
    }

    @Test
    public void equals() {
        FollowUpCommand set = new FollowUpCommand(INDEX_FIRST_PERSON, DATE);

        assertEquals(set, new FollowUpCommand(INDEX_FIRST_PERSON, DATE));
        assertNotEquals(set, new FollowUpCommand(INDEX_SECOND_PERSON, DATE));
        assertNotEquals(set, new FollowUpCommand(INDEX_FIRST_PERSON, OTHER_DATE));
        assertNotEquals(set, new FollowUpCommand(INDEX_FIRST_PERSON, null));
        assertNotEquals(set, null);
    }

    private static Model copyOf(Model source) {
        return new ModelManager(new AddressBook(source.getAddressBook()), new UserPrefs(), FIXED_CLOCK);
    }

    private Model modelWith(Person original, Person replacement) {
        AddressBook book = getTypicalAddressBook();
        book.setPerson(original, replacement);
        return new ModelManager(book, new UserPrefs(), FIXED_CLOCK);
    }

    /** Runs a command and fails the test on a {@code CommandException}. */
    private static final class FollowUpCommandRunner {
        void run(Model model, FollowUpCommand command) {
            try {
                command.execute(model);
            } catch (seedu.address.logic.commands.exceptions.CommandException e) {
                throw new AssertionError(e);
            }
        }
    }
}
