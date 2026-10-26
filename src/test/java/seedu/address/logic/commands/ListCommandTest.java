package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            model = new ModelManager(new AddressBookBuilder().withPerson(older).withPerson(CARL)
                    .withPerson(newer).build(), new UserPrefs(), FIXED_CLOCK);
            expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
            model.updateFilteredPersonList(person -> !person.equals(CARL));
            model.sortFilteredPersonList(criterion);
            assertEquals(List.of(newer, older), model.getFilteredPersonList());

            assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);

            assertEquals(List.of(older, CARL, newer), model.getFilteredPersonList());
            assertEquals("Original order - 3 contact(s)", model.getPersonListStatus().get());
            new ViewCommand(INDEX_FIRST_PERSON).execute(model);
            assertEquals(older.getName(), model.getViewedPerson().get().getName());
            assertEquals(older.getName(), model.getFilteredPersonList().getFirst().getName());
        }
    }
}
