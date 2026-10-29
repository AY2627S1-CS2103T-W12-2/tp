package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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
import seedu.address.testutil.MutableClock;
import seedu.address.testutil.PersonBuilder;

/**
 * Runs {@code followup} and {@code due} through {@link LogicManager} with real storage and a controllable clock,
 * to check activity times, failed and unchanged commands, save rollback, due-list indexes and undo/redo together.
 */
public class FollowUpFeatureTest {
    private static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);

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
                    throw new IOException("test failure");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }

    @Test
    public void execute_followUpChange_stampsOnlyLastEditedInWholeSecondUtc() throws Exception {
        logic.execute("add n/Amy Bee p/85355255 e/amy@example.com");
        Instant created = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        clock.advanceMillis(60_500);
        logic.execute("view 1");
        Instant viewed = model.getFilteredPersonList().get(0).getActivity().getLastViewedAt().orElseThrow();
        clock.advanceMillis(60_500);

        logic.execute("followup 1 d/2026-10-25");

        Person changed = model.getFilteredPersonList().get(0);
        assertEquals(Optional.of(created), changed.getActivity().getCreatedAt());
        assertEquals(Optional.of(viewed), changed.getActivity().getLastViewedAt());
        assertEquals(Optional.of(clock.instant().truncatedTo(ChronoUnit.SECONDS)),
                changed.getActivity().getLastEditedAt());
        String json = Files.readString(folder.resolve("contacts.json"));
        assertTrue(json.contains("\"createdAt\" : \"2026-10-20T01:02:03Z\""), json);
        assertTrue(json.contains("\"lastEditedAt\" : \"2026-10-20T01:04:04Z\""), json);
        assertTrue(json.contains("\"followUpDate\" : \"2026-10-25\""), json);
        assertFalse(json.matches("(?s).*\\d\\.\\d+Z.*"), json);
    }

    @Test
    public void execute_failedAndUnchangedFollowUp_stampNothingAndAddNoUndoEntry() throws Exception {
        Person amy = new PersonBuilder().withName("Amy Bee").withPhone("85355255")
                .withFollowUpDate(LocalDate.of(2026, 10, 25)).build();
        Person bob = new PersonBuilder().withName("Bob Choo").withPhone("22222222").build();
        model.addPerson(amy);
        model.addPerson(bob);
        clock.advanceMillis(60_000);

        assertThrows(ParseException.class, () -> logic.execute("followup 1 d/2026-02-30"));
        assertThrows(ParseException.class, () -> logic.execute("followup 1 d/2027-02-29"));
        assertThrows(ParseException.class, () -> logic.execute("followup 1"));
        assertThrows(ParseException.class, () -> logic.execute("followup 0 d/2026-10-25"));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> logic.execute("followup 3 d/2026-10-25")).getMessage());
        assertEquals("Amy Bee already has follow-up date 2026-10-25. Nothing was changed.",
                logic.execute("followup 1 d/2026-10-25").getFeedbackToUser());
        assertEquals("Bob Choo has no follow-up date to clear. Nothing was changed.",
                logic.execute("followup 2 clear").getFeedbackToUser());

        assertEquals(List.of(amy, bob), model.getFilteredPersonList());
        assertFalse(model.canUndo());
        assertEquals("Nothing to undo.", assertThrows(CommandException.class, () -> logic.execute("undo"))
                .getMessage());
    }

    @Test
    public void execute_setReplaceAndClear_reportExactMessages() throws Exception {
        model.addPerson(new PersonBuilder().withName("Amy Bee").withPhone("85355255").build());

        assertEquals("Follow-up date for Amy Bee set to 2026-10-25.",
                logic.execute("followup 1 d/2026-10-25").getFeedbackToUser());
        assertEquals("Follow-up date for Amy Bee set to 2028-02-29.",
                logic.execute("followup 1 d/2028-02-29").getFeedbackToUser());
        assertEquals("Follow-up date cleared for Amy Bee.", logic.execute("followup 1 clear").getFeedbackToUser());
        assertEquals("Enter a real follow-up date as YYYY-MM-DD, e.g. 2026-10-25.",
                assertThrows(ParseException.class, () -> logic.execute("followup 1 d/2027-02-29")).getMessage());
    }

    @Test
    public void execute_followUpWithFailedSave_rollsBackDateActivityAndHistory() throws Exception {
        Person amy = new PersonBuilder().withName("Amy Bee").withPhone("85355255").build();
        model.addPerson(amy);
        logic.execute("followup 1 d/2026-10-25");
        Person before = model.getFilteredPersonList().get(0);
        String fileBefore = Files.readString(folder.resolve("contacts.json"));
        clock.advanceMillis(60_000);
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("followup 1 d/2026-11-01"));
        assertThrows(CommandException.class, () -> logic.execute("followup 1 clear"));

        assertEquals(List.of(before), model.getFilteredPersonList());
        assertEquals(fileBefore, Files.readString(folder.resolve("contacts.json")));
        failSave = false;
        logic.execute("undo");
        assertEquals(Optional.empty(), model.getFilteredPersonList().get(0).getFollowUpDate());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_clearFromDueListWithFailedSave_restoresDueListAndSelection() throws Exception {
        addDue("Amy Bee", "85355255", TODAY.minusDays(3));
        addDue("Bob Choo", "22222222", TODAY);
        logic.execute("due");
        logic.execute("view 2");
        Person viewed = model.getViewedPerson().get();
        List<Person> dueList = List.copyOf(model.getFilteredPersonList());
        failSave = true;

        assertThrows(CommandException.class, () -> logic.execute("followup 2 clear"));

        assertEquals(dueList, model.getFilteredPersonList());
        assertEquals(viewed, model.getViewedPerson().get());
    }

    @Test
    public void execute_dueListIndexes_followDisplayedOrderAndShrinkAfterClear() throws Exception {
        addDue("Amy Bee", "85355255", TODAY);
        addDue("Bob Choo", "22222222", TODAY.minusDays(9));
        addDue("Cara Lim", "33333333", null);
        addDue("Dan Ng", "44444444", TODAY.plusDays(1));

        assertEquals("Due for follow-up on or before 2026-10-20.\n2 contact(s) listed!",
                logic.execute("due").getFeedbackToUser());
        assertEquals(List.of("Bob Choo", "Amy Bee"), names());
        logic.execute("view 1");
        assertEquals("Bob Choo", model.getViewedPerson().get().getName().fullName);

        // index 1 in the due list is Bob (earliest), not the first contact in the address book
        assertEquals("Follow-up date cleared for Bob Choo.", logic.execute("followup 1 clear").getFeedbackToUser());
        assertEquals(List.of("Amy Bee"), names());
        assertNull(model.getViewedPerson().get());
        assertThrows(CommandException.class, () -> logic.execute("followup 2 clear"));

        // a date that is still due keeps the contact on the list
        logic.execute("followup 1 d/2026-10-01");
        assertEquals(List.of("Amy Bee"), names());
    }

    @Test
    public void execute_replaceDateInDueList_reordersByNewDate() throws Exception {
        addDue("Amy Bee", "85355255", TODAY.minusDays(5));
        addDue("Bob Choo", "22222222", TODAY.minusDays(1));
        logic.execute("due");
        assertEquals(List.of("Amy Bee", "Bob Choo"), names());

        logic.execute("followup 2 d/2026-09-01");

        assertEquals(List.of("Bob Choo", "Amy Bee"), names());
    }

    @Test
    public void execute_dueWithNothingDue_isValidEmptyListAndKeepsData() throws Exception {
        addDue("Amy Bee", "85355255", TODAY.plusDays(1));
        addDue("Bob Choo", "22222222", null);

        assertEquals("Due for follow-up on or before 2026-10-20.\n0 contact(s) listed!",
                logic.execute("due").getFeedbackToUser());

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_followUpUndoRedo_restoreDateAndActivityWithoutNewStamp() throws Exception {
        logic.execute("add n/Amy Bee p/85355255 e/amy@example.com");
        Person added = model.getFilteredPersonList().get(0);
        clock.advanceMillis(60_000);
        logic.execute("followup 1 d/2026-10-25");
        Person withDate = model.getFilteredPersonList().get(0);
        clock.advanceMillis(60_000);
        logic.execute("followup 1 d/2026-11-01");
        Person replaced = model.getFilteredPersonList().get(0);
        clock.advanceMillis(60_000);
        logic.execute("followup 1 clear");
        Person cleared = model.getFilteredPersonList().get(0);
        clock.advanceMillis(60_000);

        logic.execute("undo");
        assertEquals(replaced, model.getFilteredPersonList().get(0));
        logic.execute("undo");
        assertEquals(withDate, model.getFilteredPersonList().get(0));
        logic.execute("undo");
        assertEquals(added, model.getFilteredPersonList().get(0));
        assertEquals(Optional.empty(), added.getFollowUpDate());
        logic.execute("redo");
        assertEquals(withDate, model.getFilteredPersonList().get(0));
        logic.execute("redo");
        assertEquals(replaced, model.getFilteredPersonList().get(0));
        logic.execute("redo");

        assertEquals(cleared, model.getFilteredPersonList().get(0));
        assertEquals(cleared, storage.readAddressBook().orElseThrow().getPersonList().get(0));
    }

    @Test
    public void execute_otherContactChanges_keepFollowUpDateAndStampOnlyTheRightTime() throws Exception {
        logic.execute("add n/Amy Bee p/85355255 e/amy@example.com");
        logic.execute("followup 1 d/2026-10-25");
        LocalDate date = LocalDate.of(2026, 10, 25);
        String[] changes = {"edit 1 e/new@example.com", "tag 1 t/mentor", "note 1 nt/Met at the demo", "star 1",
            "meeting 1 ev/Hack Singapore"};

        for (String change : changes) {
            clock.advanceMillis(60_000);
            Person before = model.getFilteredPersonList().get(0);
            logic.execute(change);
            Person after = model.getFilteredPersonList().get(0);
            assertEquals(Optional.of(date), after.getFollowUpDate(), change);
            assertEquals(before.getActivity().getCreatedAt(), after.getActivity().getCreatedAt(), change);
            assertEquals(Optional.of(clock.instant().truncatedTo(ChronoUnit.SECONDS)),
                    after.getActivity().getLastEditedAt(), change);
        }

        clock.advanceMillis(60_000);
        Person beforeView = model.getFilteredPersonList().get(0);
        logic.execute("view 1");
        Person afterView = model.getFilteredPersonList().get(0);
        assertEquals(Optional.of(date), afterView.getFollowUpDate());
        assertEquals(beforeView.getActivity().getLastEditedAt(), afterView.getActivity().getLastEditedAt());
        assertEquals(Optional.of(clock.instant().truncatedTo(ChronoUnit.SECONDS)),
                afterView.getActivity().getLastViewedAt());
    }

    private void addDue(String name, String phone, LocalDate date) {
        model.addPerson(new PersonBuilder().withName(name).withPhone(phone)
                .withEmail(name.substring(0, 3).toLowerCase() + "@example.com").withFollowUpDate(date).build());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(p -> p.getName().fullName).toList();
    }
}
