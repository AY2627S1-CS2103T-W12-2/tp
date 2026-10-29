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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies persistent filter feedback through command execution, persistence and history.
 */
public class ActiveFilterStatusTest {
    @TempDir
    private Path folder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private boolean shouldFailSave;

    @BeforeEach
    public void setUp() {
        Person alice = new PersonBuilder(ALICE).withTags("react").withStarred(true)
                .withFollowUpDate(LocalDate.of(2026, 10, 19)).build();
        Person benson = new PersonBuilder(BENSON).withTags("java").build();
        model = new ModelManager(new AddressBookBuilder().withPerson(alice).withPerson(benson).build(),
                new UserPrefs(), FIXED_CLOCK);
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldFailSave) {
                    throw new IOException("Injected save failure");
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void execute_filterThenView_retainsFilterLabelOrderAndCount() throws Exception {
        String[][] cases = {
            {"group t/REACT", "Group: react"},
            {"filter starred", "Starred only"},
            {"find Alice", "Find results"},
            {"find n/Alice", "Find results"},
            {"find t/react", "Find results"}
        };
        for (String[] testCase : cases) {
            logic.execute(testCase[0]);
            String expected = testCase[1] + " - Original order - 1 contact(s)";
            assertEquals(expected, status(), testCase[0]);

            logic.execute("view 1");

            assertEquals(expected, status(), testCase[0]);
            assertEquals(ALICE.getName(), model.getViewedPerson().get().getName());
            assertFalse(model.canUndo());
        }
    }

    @Test
    public void execute_filterChanges_replaceLabelAndKeepSort() throws Exception {
        logic.execute("sort added");
        logic.execute("group t/react");
        assertEquals("Group: react - Sorted by added - 1 contact(s)", status());
        logic.execute("filter starred");
        assertEquals("Starred only - Sorted by added - 1 contact(s)", status());
        logic.execute("find Benson");
        assertEquals("Find results - Sorted by added - 1 contact(s)", status());
        assertEquals(BENSON.getName(), model.getFilteredPersonList().getFirst().getName());
        logic.execute("filter starred off");
        assertEquals("Sorted by added - 2 contact(s)", status());
        logic.execute("list");
        assertEquals("Original order - 2 contact(s)", status());
    }

    @Test
    public void execute_noMatches_keepsFilterLabel() throws Exception {
        logic.execute("group t/missing");
        assertEquals("Group: missing - Original order - 0 contact(s)", status());
        logic.execute("find n/zzzz");
        assertEquals("Find results - Original order - 0 contact(s)", status());
        logic.execute("list");
        logic.execute("unstar 1");
        logic.execute("filter starred");
        assertEquals("Starred only - Original order - 0 contact(s)", status());
    }

    @Test
    public void execute_contactMutations_updateCountAndPreserveFilterLabel() throws Exception {
        logic.execute("group t/react");
        logic.execute("edit 1 t/java");
        assertEquals("Group: react - Original order - 0 contact(s)", status());
        logic.execute("find Benson");
        logic.execute("delete 1");
        assertEquals("Find results - Original order - 0 contact(s)", status());
        logic.execute("add n/Carl p/91234567");
        assertEquals("Find results - Original order - 0 contact(s)", status());
        logic.execute("group t/java");
        logic.execute("clear");
        assertEquals("Group: java - Original order - 0 contact(s)", status());
    }

    @Test
    public void execute_addHiddenContactUnderStarredFilter_keepsFilterAndSavesContact() throws Exception {
        logic.execute("filter starred");
        CommandResult result = logic.execute("add n/Carol p/91234567");

        assertTrue(result.getFeedbackToUser().endsWith(AddCommand.MESSAGE_HIDDEN_BY_FILTER));
        assertEquals("Starred only - Original order - 1 contact(s)", status());
        assertEquals(List.of(ALICE.getName()), model.getFilteredPersonList().stream().map(Person::getName).toList());
        // the new contact is saved even though it is hidden
        assertEquals(3, model.getAddressBook().getPersonList().size());

        logic.execute("list");
        assertEquals("Original order - 3 contact(s)", status());
    }

    @Test
    public void execute_addUnderGroupFilter_updatesCountOnlyForMatchingContact() throws Exception {
        logic.execute("group t/react");
        CommandResult shown = logic.execute("add n/Dave p/91234568 t/react");
        assertFalse(shown.getFeedbackToUser().contains(AddCommand.MESSAGE_HIDDEN_BY_FILTER));
        assertEquals("Group: react - Original order - 2 contact(s)", status());
        assertEquals(List.of(ALICE.getName().fullName, "Dave"), visibleNames());

        CommandResult hidden = logic.execute("add n/Erin p/91234569");
        assertTrue(hidden.getFeedbackToUser().endsWith(AddCommand.MESSAGE_HIDDEN_BY_FILTER));
        assertEquals("Group: react - Original order - 2 contact(s)", status());
        // Dave, who matches, is listed and Erin, who does not, is not
        assertEquals(List.of(ALICE.getName().fullName, "Dave"), visibleNames());
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addUnderFindFilter_updatesCountOnlyForMatchingContact() throws Exception {
        logic.execute("find Benson");
        assertEquals("Find results - Original order - 1 contact(s)", status());

        logic.execute("add n/Benson Lee p/91234570");
        assertEquals("Find results - Original order - 2 contact(s)", status());

        logic.execute("add n/Zed p/91234571");
        assertEquals("Find results - Original order - 2 contact(s)", status());
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addUnderDueFilter_keepsDueFilterAndSort() throws Exception {
        logic.execute("due");
        assertEquals("Due on/before 2026-10-20 - Sorted by due - 1 contact(s)", status());

        // a new contact has no follow-up date, so it is never due
        logic.execute("add n/Yan p/91234572");
        assertEquals("Due on/before 2026-10-20 - Sorted by due - 1 contact(s)", status());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_undoAndRedoAddUnderFilter_restoreFilterAndCountWithTheList() throws Exception {
        logic.execute("filter starred");
        logic.execute("add n/Carol p/91234567");
        String filteredStatus = "Starred only - Original order - 1 contact(s)";
        assertEquals(filteredStatus, status());

        logic.execute("undo");
        assertEquals(filteredStatus, status());
        assertEquals(2, model.getAddressBook().getPersonList().size());

        logic.execute("redo");
        assertEquals(filteredStatus, status());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_dueThenSort_keepsDueLabelUntilFilterIsReplaced() throws Exception {
        logic.execute("group t/java");
        logic.execute("due");
        assertEquals("Due on/before 2026-10-20 - Sorted by due - 1 contact(s)", status());
        logic.execute("sort viewed");
        logic.execute("view 1");
        assertEquals("Due on/before 2026-10-20 - Sorted by viewed - 1 contact(s)", status());
        logic.execute("find Benson");
        assertEquals("Find results - Sorted by viewed - 1 contact(s)", status());
    }

    @Test
    public void execute_failedSave_restoresFilterLabelResultsSelectionAndHistory() throws Exception {
        logic.execute("group t/react");
        logic.execute("sort viewed");
        logic.execute("view 1");
        String beforeStatus = status();
        AddressBook beforeBook = new AddressBook(model.getAddressBook());
        Person beforeSelection = model.getViewedPerson().get();
        List<Person> beforeResults = List.copyOf(model.getFilteredPersonList());
        shouldFailSave = true;

        for (String command : List.of("find Benson", "group t/java", "filter starred", "filter starred off",
                "list", "due", "sort added", "delete 1")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(beforeStatus, status(), command);
            assertEquals(beforeBook, model.getAddressBook(), command);
            assertEquals(beforeSelection, model.getViewedPerson().get(), command);
            assertEquals(beforeResults, model.getFilteredPersonList(), command);
            assertEquals(beforeBook, storage.readAddressBook().orElseThrow(), command);
            assertFalse(model.canUndo(), command);
            assertFalse(model.canRedo(), command);
        }
    }

    @Test
    public void execute_invalidFilter_preservesPreviousStatus() throws Exception {
        logic.execute("group t/react");
        for (String command : List.of("group", "filter unknown", "find n/")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals("Group: react - Original order - 1 contact(s)", status());
        }
    }

    @Test
    public void execute_undoAndRedo_restoreLabelWithFilterAndSort() throws Exception {
        logic.execute("group t/react");
        logic.execute("sort added");
        logic.execute("delete 1");
        logic.execute("filter starred");
        assertEquals("Starred only - Sorted by added - 0 contact(s)", status());

        logic.execute("undo");
        assertEquals("Group: react - Sorted by added - 1 contact(s)", status());
        assertEquals(ALICE.getName(), model.getFilteredPersonList().getFirst().getName());
        logic.execute("redo");
        assertEquals("Starred only - Sorted by added - 0 contact(s)", status());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private String status() {
        return model.getPersonListStatus().get();
    }

    private List<String> visibleNames() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
