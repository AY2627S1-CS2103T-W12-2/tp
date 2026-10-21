package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerHistoryTest {
    private static final String ADD_BOB = "add n/Bob p/87654321 e/bob@example.com";
    private static final String NOTE_AMY = "note 1 nt/Met at orientation";

    @TempDir
    private Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;
    private MutableClock clock;
    private boolean failSave;

    @BeforeEach
    public void setUp() {
        clock = new MutableClock(FIXED_CLOCK.instant(), FIXED_CLOCK.getZone());
        model = new ModelManager(new AddressBook(), new UserPrefs(), clock);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (failSave) {
                    throw new IOException("save failed");
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_undoEmptyHistory_reportsNothingToUndo() {
        CommandException error = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals("Nothing to undo.", error.getMessage());
    }

    @Test
    public void execute_undoWithArguments_rejectsArguments() {
        assertThrows(ParseException.class, () -> logic.execute("undo 1"));
    }

    @Test
    public void execute_addThenUndo_savesRestoredContacts() throws Exception {
        logic.execute(addAmy());
        assertTrue(model.canUndo());

        logic.execute("undo");

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_clearThenUndo_restoresSavedContact() throws Exception {
        logic.execute(addAmy());
        logic.execute(NOTE_AMY);
        logic.execute("view 1");
        Person selected = model.getViewedPerson().get();
        logic.execute(ADD_BOB);
        logic.execute("note 2 nt/Building a robot");
        ReadOnlyAddressBook beforeClear = new AddressBook(model.getAddressBook());
        logic.execute("clear");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        clock.advanceSeconds(60);

        logic.execute("undo");

        assertEquals(beforeClear, model.getAddressBook());
        assertEquals(selected, model.getViewedPerson().get());
        assertEquals(beforeClear, addressBookStorage.readAddressBook().orElseThrow());
        assertEquals(NOW, model.getAddressBook().getPersonList().get(0)
                .getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void execute_deleteThenUndo_restoresContactMetadataSelectionAndSavedJson() throws Exception {
        logic.execute(addAmy());
        logic.execute(NOTE_AMY);
        logic.execute("view 1");
        Person beforeDelete = model.getAddressBook().getPersonList().get(0);

        logic.execute("delete 1");
        assertNull(model.getViewedPerson().get());
        clock.advanceSeconds(60);
        logic.execute("undo");

        Person restored = model.getAddressBook().getPersonList().get(0);
        assertEquals(beforeDelete, restored);
        assertEquals(beforeDelete, model.getViewedPerson().get());
        assertEquals(beforeDelete, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));
        assertEquals(NOW, restored.getActivity().getLastViewedAt().orElseThrow());
    }

    @Test
    public void execute_deleteThenViewAnotherThenUndo_preservesLaterViewOfSurvivingContact() throws Exception {
        logic.execute(addAmy());
        logic.execute(ADD_BOB);
        logic.execute("view 1");
        Person beforeDelete = model.getAddressBook().getPersonList().get(0);
        logic.execute("delete 1");

        clock.advanceSeconds(60);
        logic.execute("view 1");
        clock.advanceSeconds(60);
        logic.execute("undo");

        Person restoredAmy = model.getAddressBook().getPersonList().get(0);
        Person survivingBob = model.getAddressBook().getPersonList().get(1);
        assertEquals(beforeDelete, restoredAmy);
        assertEquals(NOW, restoredAmy.getActivity().getLastViewedAt().orElseThrow());
        assertEquals(NOW.plusSeconds(60), survivingBob.getActivity().getLastViewedAt().orElseThrow());
        assertEquals(restoredAmy, model.getViewedPerson().get());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_addDeleteClearThenUndo_restoresOneMutationAtATime() throws Exception {
        logic.execute(addAmy());
        logic.execute(ADD_BOB);
        logic.execute("delete 1");
        logic.execute("clear");

        logic.execute("undo");
        assertEquals("Bob", model.getAddressBook().getPersonList().get(0).getName().fullName);
        logic.execute("undo");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        logic.execute("undo");
        assertEquals("Amy Bee", model.getAddressBook().getPersonList().get(0).getName().fullName);
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_clearThenUndo_restoresFilterSelectionAndValidIndexes() throws Exception {
        logic.execute(addAmy());
        logic.execute(ADD_BOB);
        logic.execute("view 1");
        Person selected = model.getViewedPerson().get();
        logic.execute("find Bob");

        logic.execute("clear");
        logic.execute("undo");

        assertEquals(selected, model.getViewedPerson().get());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Bob", model.getFilteredPersonList().get(0).getName().fullName);
        logic.execute("delete 1");
        assertEquals("Amy Bee", model.getAddressBook().getPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_clearEmptyBook_doesNotEnterHistory() throws Exception {
        logic.execute("clear");

        assertFalse(model.canUndo());
        assertEquals("Nothing to undo.", assertThrows(CommandException.class, () -> logic.execute("undo"))
                .getMessage());
    }

    @Test
    public void execute_undoThenReload_restoresJsonWithoutSessionHistory() throws Exception {
        logic.execute(addAmy());
        logic.execute("clear");
        logic.execute("undo");

        ModelManager reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(),
                new UserPrefs(), FIXED_CLOCK);

        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertFalse(reloaded.canUndo());
    }

    @Test
    public void execute_readOnlyNoOpAndView_doNotEnterHistory() throws Exception {
        logic.execute(addAmy());
        logic.execute("view 1");
        logic.execute("list");
        logic.execute("edit 1 e/amy@example.com");

        logic.execute("undo");

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_tagAlreadyPresent_doesNotEnterHistory() throws Exception {
        logic.execute(addAmy());
        logic.execute("tag 1 t/friends");
        logic.execute("tag 1 t/friends");

        logic.execute("undo");

        assertTrue(model.getAddressBook().getPersonList().get(0).getTags().isEmpty());
        assertTrue(model.canUndo());
    }

    @Test
    public void execute_editAndTagUndo_restoresOneSavedChangeAtATime() throws Exception {
        logic.execute(addAmy());
        logic.execute("view 1");
        Person original = model.getFilteredPersonList().get(0);

        logic.execute("edit 1 e/new@example.com");
        logic.execute("tag 1 t/work");
        assertEquals(Set.of(new Tag("work")), model.getViewedPerson().get().getTags());

        logic.execute("undo");
        Person afterTagUndo = model.getFilteredPersonList().get(0);
        assertEquals("new@example.com", afterTagUndo.getEmail().value);
        assertTrue(afterTagUndo.getTags().isEmpty());
        assertEquals(afterTagUndo, model.getViewedPerson().get());
        assertEquals(afterTagUndo, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));

        logic.execute("undo");
        Person afterEditUndo = model.getFilteredPersonList().get(0);
        assertEquals(original, afterEditUndo);
        assertEquals(afterEditUndo, model.getViewedPerson().get());
        assertEquals(afterEditUndo, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));
    }

    @Test
    public void execute_identityEditAndLaterView_undoPreservesViewWhenEditTimeIsUnchanged() throws Exception {
        Person previouslyEdited = AMY.withActivity(new Activity(null, NOW, null));
        model.addPerson(previouslyEdited);
        logic.execute("edit 1 n/New Amy");
        logic.execute("view 1");

        logic.execute("undo");

        Person restored = model.getFilteredPersonList().get(0);
        assertEquals(AMY.getName(), restored.getName());
        assertEquals(NOW, restored.getActivity().getLastEditedAt().orElseThrow());
        assertEquals(NOW, restored.getActivity().getLastViewedAt().orElseThrow());
        assertEquals(restored, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));
    }

    @Test
    public void execute_filteredEditAndTagUndo_restoresDisplayedIndexes() throws Exception {
        logic.execute(addAmy());
        logic.execute("add n/Bob Choo p/91234567 e/bob@example.com");
        logic.execute("find Bob");
        assertEquals(1, model.getFilteredPersonList().size());

        logic.execute("tag 1 t/work");
        logic.execute("edit 1 e/newbob@example.com");
        logic.execute("undo");

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Bob Choo", model.getFilteredPersonList().get(0).getName().fullName);
        assertEquals("bob@example.com", model.getFilteredPersonList().get(0).getEmail().value);
        logic.execute("undo");
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
    }

    @Test
    public void execute_unchangedEditAndDuplicateTag_keepFilterAndHistory() throws Exception {
        logic.execute(addAmy());
        logic.execute("add n/Bob Choo p/91234567 e/bob@example.com");
        logic.execute("find Amy");
        logic.execute("tag 1 t/work");

        logic.execute("edit 1 e/amy@example.com");
        logic.execute("tag 1 t/work");

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Amy Bee", model.getFilteredPersonList().get(0).getName().fullName);
        logic.execute("undo");
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
    }

    @Test
    public void execute_failedTagSave_keepsContactAndEarlierHistory() throws Exception {
        logic.execute(addAmy());
        Person original = model.getFilteredPersonList().get(0);
        failSave = true;

        try {
            assertThrows(CommandException.class, () -> logic.execute("tag 1 t/work"));

            assertEquals(original, model.getFilteredPersonList().get(0));
            assertEquals(original, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));
            assertTrue(model.canUndo());
            assertFalse(model.canRedo());
        } finally {
            failSave = false;
        }
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_undoEditAfterReload_savesRestoredContact() throws Exception {
        logic.execute(addAmy());
        logic.execute("edit 1 e/new@example.com");
        logic.execute("undo");

        AddressBook reloadedBook = new AddressBook(addressBookStorage.readAddressBook().orElseThrow());
        ModelManager reloadedModel = new ModelManager(reloadedBook, new UserPrefs(), FIXED_CLOCK);

        assertEquals("amy@example.com", reloadedModel.getFilteredPersonList().get(0).getEmail().value);
        assertTrue(reloadedModel.getFilteredPersonList().get(0).getActivity().getLastEditedAt().isEmpty());
        assertFalse(reloadedModel.canUndo());
    }

    @Test
    public void execute_failedSave_rollsBackContactsAndHistory() throws Exception {
        failSave = true;
        assertThrows(CommandException.class, () -> logic.execute(addAmy()));

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_failedDeleteSave_restoresContactSelectionAndHistory() throws Exception {
        logic.execute(addAmy());
        logic.execute("view 1");
        Person selected = model.getViewedPerson().get();
        failSave = true;
        try {
            assertThrows(CommandException.class, () -> logic.execute("delete 1"));

            assertEquals(selected, model.getAddressBook().getPersonList().get(0));
            assertEquals(selected, model.getViewedPerson().get());
            assertEquals(selected, addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(0));
            assertTrue(model.canUndo());
        } finally {
            failSave = false;
        }
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_failedClearSave_restoresContactsAndHistory() throws Exception {
        logic.execute(addAmy());
        failSave = true;
        try {
            assertThrows(CommandException.class, () -> logic.execute("clear"));

            assertEquals(1, model.getAddressBook().getPersonList().size());
            assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
            assertTrue(model.canUndo());
        } finally {
            failSave = false;
        }
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_failedEditSave_preservesEarlierUndoHistory() throws Exception {
        logic.execute(addAmy());
        failSave = true;

        try {
            assertThrows(CommandException.class, () -> logic.execute("edit 1 e/changed@example.com"));

            assertEquals("amy@example.com", model.getAddressBook().getPersonList().get(0).getEmail().value);
            assertEquals("amy@example.com", addressBookStorage.readAddressBook().orElseThrow()
                    .getPersonList().get(0).getEmail().value);
            assertTrue(model.canUndo());
            assertFalse(model.canRedo());
        } finally {
            failSave = false;
        }
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_undoFailedSave_restoresContactsAndHistory() throws Exception {
        logic.execute(addAmy());
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("undo"));

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(1, addressBookStorage.readAddressBook().orElseThrow().getPersonList().size());
        assertTrue(model.canUndo());
        assertFalse(model.canRedo());
    }

    @Test
    public void execute_failedCommand_doesNotEnterHistory() throws Exception {
        logic.execute(addAmy());
        AtomicInteger listChanges = new AtomicInteger();
        model.getFilteredPersonList().addListener((ListChangeListener.Change<?> change) ->
                listChanges.incrementAndGet());
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        assertEquals(0, listChanges.get());

        logic.execute("undo");
        assertFalse(model.canUndo());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_duplicateAdd_doesNotRefreshDisplayedList() throws Exception {
        logic.execute(addAmy());
        AtomicInteger listChanges = new AtomicInteger();
        model.getFilteredPersonList().addListener((ListChangeListener.Change<?> change) ->
                listChanges.incrementAndGet());

        assertThrows(CommandException.class, () -> logic.execute(addAmy()));

        assertEquals(0, listChanges.get());
    }

    @Test
    public void execute_viewWithFailedSave_restoresSelectionAndViewTime() throws Exception {
        logic.execute(addAmy());
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("view 1"));

        assertNull(model.getViewedPerson().get());
        assertTrue(model.getAddressBook().getPersonList().get(0).getActivity().getLastViewedAt().isEmpty());
    }

    @Test
    public void execute_findWithFailedSave_restoresDisplayedList() throws Exception {
        logic.execute(addAmy());
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("find UnmatchedName"));

        assertEquals(1, model.getFilteredPersonList().size());
    }

    private static String addAmy() {
        return "add" + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY;
    }

    private static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
