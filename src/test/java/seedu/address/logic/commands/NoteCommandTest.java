package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.copyOf;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.EARLIER;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {

    private static final Note NOTE = new Note("Met at booth 4, wants a React teammate");
    private static final Note OTHER_NOTE = new Note("Follow up after the demo");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

    @Test
    public void execute_setNoteOnPersonWithoutNote_success() {
        Model expectedModel = copyOf(model);
        expectedModel.editPerson(ALICE, ALICE.toBuilder().withNote(NOTE).build());

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, NOTE);
        String expectedMessage = String.format(NoteCommand.MESSAGE_SET_SUCCESS, ALICE.getName().fullName);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        Person updated = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(NOTE, updated.getNote());
        assertEquals(Optional.of(NOW), updated.getActivity().getLastEditedAt());
    }

    @Test
    public void execute_replaceExistingNote_success() {
        Person aliceWithNote = ALICE.toBuilder().withNote(NOTE).build();
        Model modelWithNote = modelWith(ALICE, aliceWithNote);
        Model expectedModel = copyOf(modelWithNote);
        expectedModel.editPerson(aliceWithNote, aliceWithNote.toBuilder().withNote(OTHER_NOTE).build());

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, OTHER_NOTE);
        String expectedMessage = String.format(NoteCommand.MESSAGE_SET_SUCCESS, ALICE.getName().fullName);

        assertCommandSuccess(command, modelWithNote, expectedMessage, expectedModel);
        assertEquals(OTHER_NOTE, modelWithNote.getFilteredPersonList().get(0).getNote());
    }

    @Test
    public void execute_clearExistingNote_success() {
        Person aliceWithNote = ALICE.toBuilder().withNote(NOTE).build();
        Model modelWithNote = modelWith(ALICE, aliceWithNote);
        Model expectedModel = copyOf(modelWithNote);
        expectedModel.editPerson(aliceWithNote, aliceWithNote.toBuilder().withNote(Note.EMPTY).build());

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, Note.EMPTY);
        String expectedMessage = String.format(NoteCommand.MESSAGE_CLEAR_SUCCESS, ALICE.getName().fullName);

        assertCommandSuccess(command, modelWithNote, expectedMessage, expectedModel);
        assertTrue(modelWithNote.getFilteredPersonList().get(0).getNote().isEmpty());
    }

    @Test
    public void execute_otherDetailsAndActivityKept_success() throws Exception {
        Person benson = BENSON.toBuilder().withActivity(new Activity(EARLIER, null, null)).build();
        Model modelWithBenson = modelWith(BENSON, benson);

        new NoteCommand(INDEX_SECOND_PERSON, NOTE).execute(modelWithBenson);

        Person updated = modelWithBenson.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(benson.getName(), updated.getName());
        assertEquals(benson.getPhone(), updated.getPhone());
        assertEquals(benson.getEmail(), updated.getEmail());
        assertEquals(benson.getLinkedIn(), updated.getLinkedIn());
        assertEquals(benson.getTags(), updated.getTags());
        assertEquals(Optional.of(EARLIER), updated.getActivity().getCreatedAt());
        assertEquals(Optional.of(NOW), updated.getActivity().getLastEditedAt());
        assertEquals(NOTE, updated.getNote());
    }

    @Test
    public void execute_sameNote_isInformationalNoOp() {
        Person aliceWithNote = new PersonBuilder(ALICE).withNote(NOTE.value).build();
        Model modelWithNote = modelWith(ALICE, aliceWithNote);
        Model expectedModel = copyOf(modelWithNote);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, NOTE);
        String expectedMessage = String.format(NoteCommand.MESSAGE_UNCHANGED, ALICE.getName().fullName);

        assertCommandSuccess(command, modelWithNote, expectedMessage, expectedModel);
        // the very same person object stays in the list, so no last-edited time was recorded
        assertSame(aliceWithNote, modelWithNote.getFilteredPersonList().get(0));
        assertFalse(modelWithNote.getFilteredPersonList().get(0).getActivity().getLastEditedAt().isPresent());
    }

    @Test
    public void execute_clearWhenNoNote_isInformationalNoOp() {
        Model expectedModel = copyOf(model);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, Note.EMPTY);
        String expectedMessage = String.format(NoteCommand.MESSAGE_NO_NOTE_TO_CLEAR, ALICE.getName().fullName);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertSame(ALICE, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_viewedPerson_followsTheNewNote() throws Exception {
        model.viewPerson(model.getFilteredPersonList().get(0));

        new NoteCommand(INDEX_FIRST_PERSON, NOTE).execute(model);

        assertEquals(NOTE, model.getViewedPerson().get().getNote());
        assertSame(model.getFilteredPersonList().get(0), model.getViewedPerson().get());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new NoteCommand(outOfBoundIndex, NOTE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new NoteCommand(outOfBoundIndex, Note.EMPTY), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person benson = model.getFilteredPersonList().get(0);
        Model expectedModel = copyOf(model);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.editPerson(benson, benson.toBuilder().withNote(NOTE).build());

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, NOTE);
        String expectedMessage = String.format(NoteCommand.MESSAGE_SET_SUCCESS, BENSON.getName().fullName);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new NoteCommand(INDEX_SECOND_PERSON, NOTE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new NoteCommand(INDEX_FIRST_PERSON, NOTE), model, Messages.MESSAGE_EMPTY_PERSON_LIST);
    }

    @Test
    public void equals() {
        NoteCommand setFirst = new NoteCommand(INDEX_FIRST_PERSON, NOTE);

        // same object, same values
        assertTrue(setFirst.equals(setFirst));
        assertTrue(setFirst.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE.value))));

        // different type, null
        assertFalse(setFirst.equals(1));
        assertFalse(setFirst.equals(null));

        // different index, different note, clear
        assertFalse(setFirst.equals(new NoteCommand(INDEX_SECOND_PERSON, NOTE)));
        assertFalse(setFirst.equals(new NoteCommand(INDEX_FIRST_PERSON, OTHER_NOTE)));
        assertFalse(setFirst.equals(new NoteCommand(INDEX_FIRST_PERSON, Note.EMPTY)));
    }

    @Test
    public void toStringMethod() {
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, NOTE);
        String expected = NoteCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", note=[" + NOTE.value.length() + " characters]}";

        assertEquals(expected, command.toString());
        // the text of the note is personal and must not reach the log through toString
        assertFalse(command.toString().contains(NOTE.value));
    }

    /** Returns a model like the typical one, but with {@code target} replaced by {@code replacement}. */
    private static Model modelWith(Person target, Person replacement) {
        AddressBook addressBook = getTypicalAddressBook();
        addressBook.setPerson(target, replacement);
        return new ModelManager(addressBook, new UserPrefs(), FIXED_CLOCK);
    }
}
