package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerHistoryTest {

    private final ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);

    @Test
    public void undo_emptyHistory_reportsEmptyHistory() {
        assertFalse(model.canUndo());
        assertEquals("Nothing to undo.", org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                model::undo).getMessage());
    }

    @Test
    public void record_oneMutation_undoAndRedoRestoreContacts() {
        Model.Snapshot before = model.createSnapshot();
        model.addPerson(ALICE);
        model.recordSuccessfulMutation(before);

        assertTrue(model.canUndo());
        model.undo();
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.canRedo());

        model.redo();
        assertEquals(ALICE, model.getAddressBook().getPersonList().get(0));
        assertFalse(model.canRedo());
    }

    @Test
    public void record_fiftyOneMutations_keepsOnlyFiftyEntries() {
        for (int i = 0; i < 51; i++) {
            Model.Snapshot before = model.createSnapshot();
            model.addPerson(new PersonBuilder().withName("Person " + i)
                    .withPhone(String.format("8%07d", i)).build());
            model.recordSuccessfulMutation(before);
        }

        for (int i = 0; i < 50; i++) {
            model.undo();
        }
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertFalse(model.canUndo());
    }

    @Test
    public void record_fiftyMutations_allFiftyCanBeUndone() {
        for (int i = 0; i < 50; i++) {
            Model.Snapshot before = model.createSnapshot();
            model.addPerson(new PersonBuilder().withName("Person " + i)
                    .withPhone(String.format("8%07d", i)).build());
            model.recordSuccessfulMutation(before);
        }

        for (int i = 0; i < 50; i++) {
            assertTrue(model.canUndo());
            model.undo();
        }
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndo());
    }

    @Test
    public void record_noOpAndView_onlyContactChangesEnterHistory() {
        model.addPerson(ALICE);
        Model.Snapshot before = model.createSnapshot();
        model.editPerson(ALICE, ALICE);
        model.recordSuccessfulMutation(before);
        model.viewPerson(ALICE);
        model.recordSuccessfulMutation(before);

        assertFalse(model.canUndo());
        assertEquals(NOW, model.getAddressBook().getPersonList().get(0).getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void undo_restoresSelectionAndActiveFilter() {
        model.addPerson(ALICE);
        model.setViewedPerson(ALICE);
        model.updateFilteredPersonList(person -> person.isSamePerson(ALICE));
        Model.Snapshot before = model.createSnapshot();
        model.addPerson(BOB);
        model.recordSuccessfulMutation(before);

        model.undo();
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(ALICE, model.getFilteredPersonList().get(0));
        assertEquals(ALICE, model.getViewedPerson().get());
    }

    @Test
    public void undo_editPreservesLaterExplicitViewTime() {
        model.addPerson(ALICE);
        Model.Snapshot before = model.createSnapshot();
        Person edited = ALICE.toBuilder().withPhone(new Phone("81234567")).build();
        model.editPerson(ALICE, edited);
        model.recordSuccessfulMutation(before);
        model.viewPerson(model.getAddressBook().getPersonList().get(0));

        model.undo();
        Person restored = model.getAddressBook().getPersonList().get(0);
        assertEquals(ALICE.getPhone(), restored.getPhone());
        assertEquals(NOW, restored.getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void undo_deleteRestoresDeletedContactOriginalViewTime() {
        Instant earlierView = NOW.minusSeconds(60);
        Person viewed = ALICE.withActivity(new Activity(null, null, earlierView));
        model.addPerson(viewed);
        Model.Snapshot before = model.createSnapshot();
        model.deletePerson(viewed);
        model.recordSuccessfulMutation(before);

        model.undo();
        assertEquals(earlierView,
                model.getAddressBook().getPersonList().get(0).getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void undo_preservesViewsOfOtherContacts() {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        Model.Snapshot before = model.createSnapshot();
        model.editPerson(ALICE, ALICE.toBuilder().withPhone(new Phone("81234567")).build());
        model.recordSuccessfulMutation(before);
        model.viewPerson(BOB);

        model.undo();
        assertEquals(NOW,
                model.getAddressBook().getPersonList().get(1).getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void undo_replacingAllContactsDoesNotTransferUnrelatedViewTime() {
        Instant originalView = NOW.minusSeconds(120);
        model.addPerson(ALICE.withActivity(new Activity(null, null, originalView)));
        Model.Snapshot before = model.createSnapshot();
        AddressBook replacement = new AddressBook();
        replacement.addPerson(BOB.withActivity(new Activity(null, null, NOW)));
        model.setAddressBook(replacement);
        model.recordSuccessfulMutation(before);

        model.undo();
        assertEquals(originalView,
                model.getAddressBook().getPersonList().get(0).getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void undo_reorderedEditDoesNotTransferAnotherContactsViewTime() {
        Instant originalView = NOW.minusSeconds(120);
        model.addPerson(ALICE.withActivity(new Activity(null, null, originalView)));
        model.addPerson(BOB);
        Model.Snapshot before = model.createSnapshot();
        AddressBook replacement = new AddressBook();
        replacement.addPerson(BOB.withActivity(new Activity(null, NOW, NOW)));
        replacement.addPerson(ALICE.toBuilder().withPhone(new Phone("81234567")).build()
                .withActivity(new Activity(null, NOW, null)));
        model.setAddressBook(replacement);
        model.recordSuccessfulMutation(before);

        model.undo();

        assertEquals(originalView,
                model.getAddressBook().getPersonList().get(0).getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void record_mutationAfterUndo_discardsRedo() {
        Model.Snapshot before = model.createSnapshot();
        model.addPerson(ALICE);
        model.recordSuccessfulMutation(before);
        model.undo();
        assertTrue(model.canRedo());

        Model.Snapshot replacementBefore = model.createSnapshot();
        model.addPerson(BOB);
        model.recordSuccessfulMutation(replacementBefore);

        assertFalse(model.canRedo());
        assertEquals(BOB, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void restore_failedUndoRestoresContactsAndHistoryStacks() {
        Model.Snapshot before = model.createSnapshot();
        model.addPerson(ALICE);
        model.recordSuccessfulMutation(before);
        Model.Snapshot beforeUndo = model.createSnapshot();
        model.undo();

        model.restore(beforeUndo);
        assertEquals(ALICE, model.getAddressBook().getPersonList().get(0));
        assertTrue(model.canUndo());
        assertFalse(model.canRedo());
    }
}
