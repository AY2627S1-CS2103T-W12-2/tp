package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerRedoTest {
    private static final String ADD_AMY = "add n/Amy Bee p/87654321 e/amy@example.com";
    private static final String ADD_BOB = "add n/Bob Choo p/91234567 e/bob@example.com";

    @TempDir
    private Path folder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private MutableClock clock;
    private boolean failSave;

    @BeforeEach
    public void setUp() {
        clock = new MutableClock(FIXED_CLOCK.instant(), FIXED_CLOCK.getZone());
        model = new ModelManager(new AddressBook(), new UserPrefs(), clock);
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave) {
                    throw new IOException("save failed");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }

    @Test
    public void execute_emptyRedoAndArguments_reportClearErrors() throws Exception {
        assertEquals("Nothing to redo.", assertThrows(CommandException.class, () -> logic.execute("redo"))
                .getMessage());
        assertThrows(ParseException.class, () -> logic.execute("redo 1"));
        logic.execute(ADD_AMY);
        assertEquals("Nothing to redo.", assertThrows(CommandException.class, () -> logic.execute("redo"))
                .getMessage());
    }

    @Test
    public void execute_addDeleteClearUndoRedoChain_restoresEachSavedState() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute(ADD_BOB);
        logic.execute("delete 1");
        logic.execute("clear");

        for (int i = 0; i < 4; i++) {
            logic.execute("undo");
        }
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.canRedo());

        logic.execute("redo");
        assertEquals("Amy Bee", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("redo");
        assertEquals(2, model.getFilteredPersonList().size());
        logic.execute("redo");
        assertEquals("Bob Choo", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("redo");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertFalse(model.canRedo());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals("Nothing to redo.", assertThrows(CommandException.class, () -> logic.execute("redo"))
                .getMessage());
        logic.execute("undo");
        assertEquals("Bob Choo", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_editTagAndNewFieldsRedo_restoresMetadataWithoutNewTimestamp() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("view 1");
        logic.execute("edit 1 e/new@example.com");
        logic.execute("tag 1 t/work");
        logic.execute("note 1 nt/Met at orientation");
        logic.execute("star 1");
        logic.execute("followup 1 d/2026-10-25");
        logic.execute("meeting 1 ev/School md/2026-10-20 r/Mentor");
        Person changed = model.getFilteredPersonList().getFirst();

        for (int i = 0; i < 6; i++) {
            logic.execute("undo");
        }
        clock.advanceSeconds(60);
        for (int i = 0; i < 6; i++) {
            logic.execute("redo");
        }

        assertEquals(changed, model.getFilteredPersonList().getFirst());
        assertEquals(changed, model.getViewedPerson().get());
        assertEquals(changed, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_redoRestoresFilterSortSelectionAndDisplayedIndexes() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute(ADD_BOB);
        logic.execute("view 1");
        logic.execute("find Bob");
        logic.execute("sort added");
        logic.execute("tag 1 t/work");
        logic.execute("undo");
        logic.execute("list");
        logic.execute("find Amy");

        logic.execute("redo");

        assertEquals("Bob Choo", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals("Sorted by added - 1 contact(s)", model.getPersonListStatus().get());
        assertEquals("Amy Bee", model.getViewedPerson().get().getName().fullName);
        assertEquals(1, model.getFilteredPersonList().getFirst().getTags().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_viewNoOpAndFailedCommand_preserveRedoUntilNewMutation() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("edit 1 e/new@example.com");
        logic.execute("undo");
        logic.execute("view 1");
        logic.execute("edit 1 e/amy@example.com");
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        assertTrue(model.canRedo());

        logic.execute("redo");
        assertEquals("new@example.com", model.getFilteredPersonList().getFirst().getEmail().value);
        assertTrue(model.getFilteredPersonList().getFirst().getActivity().getLastViewedAt().isPresent());
        logic.execute("undo");
        logic.execute("tag 1 t/new");
        assertFalse(model.canRedo());
    }

    @Test
    public void execute_failedMutationSave_doesNotClearRedo() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("undo");
        failSave = true;
        assertThrows(CommandException.class, () -> logic.execute(ADD_BOB));
        failSave = false;

        assertTrue(model.canRedo());
        logic.execute("redo");
        assertEquals("Amy Bee", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_failedRedoSave_restoresContactAndBothStacks() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("undo");
        failSave = true;
        assertThrows(CommandException.class, () -> logic.execute("redo"));

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertFalse(model.canUndo());
        assertTrue(model.canRedo());
        failSave = false;
        logic.execute("redo");
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_redoThenReload_persistsDataWithoutSessionHistory() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("delete 1");
        logic.execute("undo");
        logic.execute("redo");

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(),
                new UserPrefs(), FIXED_CLOCK);
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertNull(reloaded.getViewedPerson().get());
        assertFalse(reloaded.canUndo());
        assertFalse(reloaded.canRedo());
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
