package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.MutableClock;

/** Checks that newly added contact fields use the same saved, session-only undo history. */
public class NewContactUndoIntegrationTest {
    @TempDir
    private Path folder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private MutableClock clock;
    private boolean failSave;

    @BeforeEach
    public void setUp() throws IOException {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        clock = new MutableClock(FIXED_CLOCK.instant(), FIXED_CLOCK.getZone());
        model = new ModelManager(book, new UserPrefs(), clock);
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
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))), new PhotoStorage(folder));
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("first.png").toFile());
        ImageIO.write(new BufferedImage(9, 9, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("second.png").toFile());
    }

    @Test
    public void execute_noteSetReplaceClear_undoesEachSavedChange() throws Exception {
        Person original = current();
        logic.execute("note 1 nt/First note");
        Person first = current();
        logic.execute("note 1 nt/Second note");
        Person second = current();
        logic.execute("note 1 clear");

        assertRestoredAfterUndo(second);
        assertRestoredAfterUndo(first);
        assertRestoredAfterUndo(original);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_starAndUnstar_undoesEachSavedChange() throws Exception {
        Person original = current();
        logic.execute("star 1");
        Person starred = current();
        logic.execute("unstar 1");

        assertRestoredAfterUndo(starred);
        assertRestoredAfterUndo(original);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_followUpSetReplaceClear_undoesEachSavedChange() throws Exception {
        Person original = current();
        logic.execute("followup 1 d/2026-10-25");
        Person first = current();
        logic.execute("followup 1 d/2026-10-26");
        Person second = current();
        logic.execute("followup 1 clear");

        assertRestoredAfterUndo(second);
        assertRestoredAfterUndo(first);
        assertRestoredAfterUndo(original);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_meetingSetReplaceAndClearFields_undoesEachSavedChange() throws Exception {
        Person original = current();
        logic.execute("meeting 1 ev/Hack md/2026-10-25 r/Mentor");
        Person first = current();
        logic.execute("meeting 1 r/Participant");
        Person second = current();
        logic.execute("meeting 1 clear date");
        Person withoutDate = current();
        logic.execute("meeting 1 clear event");
        Person withoutEvent = current();
        logic.execute("meeting 1 clear role");

        assertRestoredAfterUndo(withoutEvent);
        assertRestoredAfterUndo(withoutDate);
        assertRestoredAfterUndo(second);
        assertRestoredAfterUndo(first);
        assertRestoredAfterUndo(original);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_meetingClearAll_undoRestoresAllFieldsAndSavedJson() throws Exception {
        logic.execute("meeting 1 ev/Hack md/2026-10-25 r/Mentor");
        Person complete = current();
        logic.execute("meeting 1 clear");
        assertTrue(current().getMeeting().isEmpty());

        assertRestoredAfterUndo(complete);
        assertFalse(current().getMeeting().isEmpty());
    }

    @Test
    public void execute_photoAttachReplaceClear_undoAndRedoRetainReferencesAndFiles() throws Exception {
        Person original = current();
        logic.execute("photo 1 f/first.png");
        Person first = current();
        logic.execute("photo 1 f/second.png");
        Person second = current();
        logic.execute("photo 1 clear");

        assertRestoredAfterUndo(second);
        assertRestoredAfterUndo(first);
        assertRestoredAfterUndo(original);
        assertTrue(Files.exists(folder.resolve(first.getPhotoPath())));
        assertTrue(Files.exists(folder.resolve(second.getPhotoPath())));

        model.redo();
        assertEquals(first.getPhotoPath(), current().getPhotoPath());
        model.redo();
        assertEquals(second.getPhotoPath(), current().getPhotoPath());
        model.redo();
        assertTrue(current().getPhotoPath().isEmpty());
    }

    @Test
    public void execute_deletePhotographedContact_undoRestoresReferenceAndRetainedFile() throws Exception {
        logic.execute("photo 1 f/first.png");
        logic.execute("view 1");
        Person photographed = current();
        String reference = photographed.getPhotoPath();

        logic.execute("delete 1");
        assertEquals(BENSON, current());
        assertTrue(Files.exists(folder.resolve(reference)));

        logic.execute("undo");
        assertEquals(photographed, current());
        assertEquals(photographed, model.getViewedPerson().get());
        assertEquals(photographed, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        assertTrue(Files.exists(folder.resolve(reference)));
    }

    @Test
    public void execute_mixedCommandsAndClear_undoRestoresMetadataSelectionFilterAndJson() throws Exception {
        logic.execute("view 1");
        logic.execute("note 2 nt/Benson follow-up");
        logic.execute("find Alice");
        Person original = current();
        logic.execute("note 1 nt/Teammate");
        Person noted = current();
        logic.execute("star 1");
        Person starred = current();
        logic.execute("meeting 1 ev/Hack");
        Person meeting = current();
        logic.execute("photo 1 f/first.png");
        Person photographed = current();
        logic.execute("followup 1 d/2026-10-25");
        Person followedUp = current();
        logic.execute("edit 1 e/new@example.com");
        Person edited = current();
        AddressBook beforeClear = new AddressBook(model.getAddressBook());
        logic.execute("clear");

        assertRestoredAfterUndo(edited);
        assertEquals(beforeClear, model.getAddressBook());
        assertEquals(beforeClear, storage.readAddressBook().orElseThrow());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(edited, model.getViewedPerson().get());
        assertRestoredAfterUndo(followedUp);
        assertRestoredAfterUndo(photographed);
        assertRestoredAfterUndo(meeting);
        assertRestoredAfterUndo(starred);
        assertRestoredAfterUndo(noted);
        assertRestoredAfterUndo(original);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_failedPhotoUndo_keepsBothHistoryStacksAndSavedReference() throws Exception {
        logic.execute("photo 1 f/first.png");
        Person attached = current();
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("undo"));

        assertEquals(attached, current());
        assertTrue(model.canUndo());
        assertFalse(model.canRedo());
        assertEquals(attached, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        failSave = false;
        assertRestoredAfterUndo(ALICE);
    }

    @Test
    public void execute_failedUndoWithBothStacksPresent_restoresDataAndHistory() throws Exception {
        logic.execute("note 1 nt/Teammate");
        logic.execute("star 1");
        logic.execute("undo");
        Person beforeFailure = current();
        assertTrue(model.canUndo());
        assertTrue(model.canRedo());
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("undo"));

        assertEquals(beforeFailure, current());
        assertEquals(beforeFailure, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        assertTrue(model.canUndo());
        assertTrue(model.canRedo());
        failSave = false;
        assertRestoredAfterUndo(ALICE);
    }

    @Test
    public void execute_undoNewFieldsThenReload_restoresDataWithoutSessionHistory() throws Exception {
        logic.execute("note 1 nt/Teammate");
        logic.execute("star 1");
        logic.execute("followup 1 d/2026-10-25");
        logic.execute("meeting 1 ev/Hack");
        logic.execute("photo 1 f/first.png");
        logic.execute("undo");
        logic.execute("undo");

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(),
                new UserPrefs(), FIXED_CLOCK);
        Person reloadedFirst = reloaded.getFilteredPersonList().getFirst();

        assertEquals(current(), reloadedFirst);
        assertEquals("Teammate", reloadedFirst.getNote().value);
        assertTrue(reloadedFirst.isStarred());
        assertTrue(reloadedFirst.getFollowUpDate().isPresent());
        assertTrue(reloadedFirst.getPhotoPath().isEmpty());
        assertTrue(reloadedFirst.getMeeting().isEmpty());
        assertFalse(reloaded.canUndo());
        assertFalse(reloaded.canRedo());
    }

    @Test
    public void execute_newFieldNoOpsAndNonMutationCommands_doNotDisplaceEarlierUndo() throws Exception {
        Person original = current();
        clock.advanceSeconds(60);
        logic.execute("note 1 nt/Teammate");
        Person noted = current();
        logic.execute("note 1 nt/Teammate");
        clock.advanceSeconds(60);
        logic.execute("star 1");
        Person starred = current();
        logic.execute("star 1");
        clock.advanceSeconds(60);
        logic.execute("followup 1 d/2026-10-25");
        Person followedUp = current();
        logic.execute("followup 1 d/2026-10-25");
        clock.advanceSeconds(60);
        logic.execute("meeting 1 ev/Hack");
        Person meeting = current();
        logic.execute("meeting 1 ev/Hack");
        clock.advanceSeconds(60);
        logic.execute("photo 1 f/first.png");
        logic.execute("photo 1 f/first.png");
        clock.advanceSeconds(60);
        logic.execute("view 1");
        Instant viewedAt = current().getActivity().getLastViewedAt().orElseThrow();
        logic.execute("list");

        assertNotEquals(noted.getActivity().getLastEditedAt(), starred.getActivity().getLastEditedAt());
        assertNotEquals(starred.getActivity().getLastEditedAt(), followedUp.getActivity().getLastEditedAt());
        assertTrue(model.canUndo());
        assertRestoredAfterUndo(withViewAt(meeting, viewedAt));
        assertTrue(current().getPhotoPath().isEmpty());
        assertRestoredAfterUndo(withViewAt(followedUp, viewedAt));
        assertRestoredAfterUndo(withViewAt(starred, viewedAt));
        assertRestoredAfterUndo(withViewAt(noted, viewedAt));
        assertRestoredAfterUndo(withViewAt(original, viewedAt));
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_laterViewAndMissingPhoto_undoPreservesViewAndSafelyRestoresReference() throws Exception {
        logic.execute("photo 1 f/first.png");
        String reference = current().getPhotoPath();
        logic.execute("photo 1 clear");
        logic.execute("view 1");
        Files.delete(folder.resolve(reference));

        logic.execute("undo");

        assertEquals(reference, current().getPhotoPath());
        assertEquals(FIXED_CLOCK.instant().truncatedTo(ChronoUnit.SECONDS),
                current().getActivity().getLastViewedAt().orElseThrow());
        assertEquals(current(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        assertTrue(new PhotoStorage(folder).readPhoto(reference).isEmpty());
        assertEquals(BENSON, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void execute_photoAfterSort_undoRestoresSortSelectionAndDisplayedIndexes() throws Exception {
        logic.execute("view 2");
        logic.execute("sort viewed");
        assertEquals(BENSON.getName(), model.getFilteredPersonList().getFirst().getName());
        Person selected = model.getViewedPerson().get();
        logic.execute("photo 1 f/first.png");
        logic.execute("list");

        logic.execute("undo");

        assertEquals(BENSON.getName(), model.getFilteredPersonList().getFirst().getName());
        assertEquals("Sorted by viewed - 2 contact(s)", model.getPersonListStatus().get());
        assertEquals(selected, model.getViewedPerson().get());
        assertTrue(model.getFilteredPersonList().getFirst().getPhotoPath().isEmpty());
        logic.execute("delete 1");
        assertEquals(ALICE.getName(), model.getFilteredPersonList().getFirst().getName());
    }

    private Person current() {
        return model.getAddressBook().getPersonList().getFirst();
    }

    private void assertRestoredAfterUndo(Person expected) throws Exception {
        logic.execute("undo");
        assertEquals(expected, current());
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    private static Person withViewAt(Person person, Instant viewedAt) {
        return person.withActivity(person.getActivity().withLastViewedAt(viewedAt));
    }
}
