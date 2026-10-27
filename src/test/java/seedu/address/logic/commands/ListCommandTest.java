package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.EARLIEST;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.SortCriterion;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filteredAndSortedList_restoresOriginalOrderAndIndexes() throws Exception {
        Person older = new PersonBuilder(ALICE).withActivity(new Activity(EARLIEST, EARLIEST, EARLIEST))
                .withFollowUpDate(LocalDate.of(2026, 10, 20)).build();
        Person newer = new PersonBuilder(BENSON).withActivity(new Activity(NOW, NOW, NOW))
                .withFollowUpDate(LocalDate.of(2026, 10, 19)).build();
        for (SortCriterion criterion : SortCriterion.values()) {
            assertNotEquals(0, criterion.getComparator().compare(older, newer),
                    "Fixtures must have different sort keys for " + criterion);
            List<Person> sortedPersons = List.of(older, newer).stream().sorted(criterion.getComparator()).toList();
            Person originalFirst = sortedPersons.getLast();
            Person originalLast = sortedPersons.getFirst();
            // Start opposite to this criterion's order so list must undo a real reorder.
            model = new ModelManager(new AddressBookBuilder().withPerson(originalFirst).withPerson(CARL)
                    .withPerson(originalLast).build(), new UserPrefs(), FIXED_CLOCK);
            expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
            model.updateFilteredPersonList(person -> !person.equals(CARL));
            model.sortFilteredPersonList(criterion);
            assertEquals(sortedPersons, model.getFilteredPersonList());

            assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);

            assertEquals(List.of(originalFirst, CARL, originalLast), model.getFilteredPersonList());
            assertEquals("Original order - 3 contact(s)", model.getPersonListStatus().get());
            new ViewCommand(INDEX_FIRST_PERSON).execute(model);
            assertEquals(originalFirst.getName(), model.getViewedPerson().get().getName());
            assertEquals(originalFirst.getName(), model.getFilteredPersonList().getFirst().getName());
        }
    }
}
