package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Meeting;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MeetingFeatureTest {
    @TempDir
    private Path folder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private boolean failSave;

    @BeforeEach
    public void setUp() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        model = new ModelManager(book, new UserPrefs(), FIXED_CLOCK);
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave) {
                    throw new IOException("test failure");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }

    @Test
    public void execute_partialUpdateAndClear_preservesOtherFieldsAndReloads() throws Exception {
        logic.execute("meeting 1 ev/Hack Singapore md/2024-02-29 r/Mentor");
        logic.execute("meeting 1 r/Participant");
        Meeting meeting = model.getFilteredPersonList().getFirst().getMeeting();
        assertEquals("Hack Singapore", meeting.getEventName().orElseThrow());
        assertEquals(LocalDate.of(2024, 2, 29), meeting.getDate().orElseThrow());
        assertEquals("Participant", meeting.getRole().orElseThrow());
        assertEquals(ALICE.getTags(), model.getFilteredPersonList().getFirst().getTags());
        assertEquals(meeting, storage.readAddressBook().orElseThrow().getPersonList().getFirst().getMeeting());
        for (String field : new String[]{"event", "date", "role"}) {
            logic.execute("meeting 1 clear " + field);
        }
        assertTrue(model.getFilteredPersonList().getFirst().getMeeting().isEmpty());
    }

    @Test
    public void execute_meetingUpdate_preservesExistingNote() throws Exception {
        logic.execute("note 1 nt/Met at the AI track");
        logic.execute("meeting 1 ev/Hack Singapore");
        assertEquals("Met at the AI track", model.getFilteredPersonList().getFirst().getNote().value);
        assertEquals("Met at the AI track", storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getNote().value);
    }

    @Test
    public void execute_noOp_doesNotChangeTimeOrHistory() throws Exception {
        logic.execute("meeting 1 clear");
        assertFalse(model.canUndo());
        assertEquals(ALICE.getActivity(), model.getFilteredPersonList().getFirst().getActivity());
        logic.execute("meeting 1 ev/Hack");
        logic.execute("meeting 1 ev/Hack");
        logic.execute("undo");
        assertFalse(model.canUndo());
        assertTrue(model.getFilteredPersonList().getFirst().getMeeting().isEmpty());
    }

    @Test
    public void execute_filteredIndexesAndEventSearch_useVisibleContacts() throws Exception {
        logic.execute("find Benson");
        logic.execute("meeting 1 ev/Hack Singapore");
        assertEquals("Hack Singapore", model.getFilteredPersonList().getFirst().getMeeting()
                .getEventName().orElseThrow());
        logic.execute("find ev/sInGa");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getName(), model.getFilteredPersonList().getFirst().getName());
        logic.execute("find ev/absent");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_slashContainingEvent_canBeFound() throws Exception {
        logic.execute("meeting 1 ev/Conference c/o NUS");
        logic.execute("find ev/Conference c/o NUS");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(ALICE.getName(), model.getFilteredPersonList().getFirst().getName());
    }

    @Test
    public void execute_invalidInputs_leaveContactsUnchanged() {
        for (String args : new String[]{"", "1", "0 ev/Hack", "-1 ev/Hack", "1 ev/", "1 r/ ",
            "1 md/2026-02-30", "1 clear other", "1 clear ev/Hack", "1 ev/A ev/B"}) {
            assertThrows(ParseException.class, () -> logic.execute("meeting " + args));
        }
        assertThrows(CommandException.class, () -> logic.execute("meeting 3 ev/Hack"));
        assertFalse(model.canUndo());
        assertEquals(ALICE, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_failedSave_rollsBackDetailsSelectionAndHistory() throws Exception {
        logic.execute("view 1");
        var before = model.getViewedPerson().get();
        failSave = true;
        assertThrows(CommandException.class, () -> logic.execute("meeting 1 ev/Hack"));
        assertEquals(before, model.getViewedPerson().get());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_editDifferentContactOutsideViewedFilter_preservesViewedContact() throws Exception {
        logic.execute("view 1");
        var viewedAlice = model.getViewedPerson().get();
        logic.execute("find Benson");
        logic.execute("meeting 1 ev/Hack");
        assertEquals(viewedAlice, model.getViewedPerson().get());
        assertEquals("Hack", model.getFilteredPersonList().getFirst().getMeeting().getEventName().orElseThrow());
    }

    @Test
    public void execute_editViewedContactStillMatchingFilter_keepsUpdatedSelection() throws Exception {
        logic.execute("meeting 1 ev/Hack");
        logic.execute("find ev/Hack");
        logic.execute("view 1");
        logic.execute("meeting 1 r/Mentor");
        assertEquals(model.getFilteredPersonList().getFirst(), model.getViewedPerson().get());
        assertEquals("Mentor", model.getViewedPerson().get().getMeeting().getRole().orElseThrow());
    }

    @Test
    public void execute_eventChangeRemovesViewedContactFromFilter_clearsSelection() throws Exception {
        logic.execute("meeting 1 ev/Hack");
        logic.execute("find ev/Hack");
        logic.execute("view 1");
        logic.execute("meeting 1 ev/Conference");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(null, model.getViewedPerson().get());
    }

    @Test
    public void execute_clearAllAndMalformedClearForms_handleExplicitRemoval() throws Exception {
        logic.execute("meeting 1 ev/Hack md/2026-10-20 r/Mentor");
        logic.execute("view 1");
        logic.execute("meeting 1 clear");
        assertTrue(model.getFilteredPersonList().getFirst().getMeeting().isEmpty());
        assertTrue(model.getViewedPerson().get().getMeeting().isEmpty());
        for (String args : new String[]{"1 unknown", "1 clear event extra"}) {
            assertThrows(ParseException.class, () -> logic.execute("meeting " + args));
        }
    }

}
