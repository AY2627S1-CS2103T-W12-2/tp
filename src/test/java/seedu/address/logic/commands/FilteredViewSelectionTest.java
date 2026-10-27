package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that list filtering preserves the detail selection without changing displayed-index targeting.
 */
public class FilteredViewSelectionTest {
    private static final List<String> BENSON_FILTERS = List.of(
            "find Benson", "find n/Benson", "group t/work", "filter starred");
    private static final List<String> EMPTY_FILTERS = List.of(
            "find zzzz", "find n/zzzz", "group t/nonexist", "filter starred");

    @Test
    public void execute_hiddenViewedContact_keepsSelectionAndContactData() throws Exception {
        for (String command : BENSON_FILTERS) {
            Model model = createModel(true);
            execute("view 1", model);
            Person viewed = model.getViewedPerson().get();
            AddressBook before = new AddressBook(model.getAddressBook());

            execute(command, model);

            assertEquals(List.of(before.getPersonList().get(1)), model.getFilteredPersonList(), command);
            assertSame(viewed, model.getViewedPerson().get(), command);
            assertEquals(before, model.getAddressBook(), command);

            execute("list", model);
            assertSame(viewed, model.getViewedPerson().get(), command);
            assertEquals(before.getPersonList(), model.getFilteredPersonList(), command);
        }
    }

    @Test
    public void execute_emptyResults_keepsSelectionEvenAfterInvalidView() throws Exception {
        for (String command : EMPTY_FILTERS) {
            Model model = createModel(false);
            execute("view 1", model);
            Person viewed = model.getViewedPerson().get();

            execute(command, model);

            assertTrue(model.getFilteredPersonList().isEmpty(), command);
            assertSame(viewed, model.getViewedPerson().get(), command);
            CommandException exception = assertThrows(CommandException.class, () -> execute("view 1", model));
            assertEquals(Messages.MESSAGE_EMPTY_PERSON_LIST, exception.getMessage(), command);
            assertSame(viewed, model.getViewedPerson().get(), command);

            execute("clear", model);
            assertNull(model.getViewedPerson().get(), command);
        }
    }

    @Test
    public void execute_matchingViewedContact_keepsSelection() throws Exception {
        for (String command : BENSON_FILTERS) {
            Model model = createModel(true);
            execute("view 2", model);
            Person viewed = model.getViewedPerson().get();

            execute(command, model);

            assertEquals(List.of(viewed), model.getFilteredPersonList(), command);
            assertSame(viewed, model.getViewedPerson().get(), command);
        }
    }

    @Test
    public void execute_noViewedContact_doesNotSelectAResult() throws Exception {
        for (String command : BENSON_FILTERS) {
            Model model = createModel(true);

            execute(command, model);

            assertEquals(1, model.getFilteredPersonList().size(), command);
            assertNull(model.getViewedPerson().get(), command);
        }
    }

    @Test
    public void execute_viewAfterFiltering_usesDisplayedIndexAndReplacesSelection() throws Exception {
        for (String command : BENSON_FILTERS) {
            Model model = createModel(true);
            execute("view 1", model);
            execute(command, model);
            Person displayed = model.getFilteredPersonList().getFirst();

            execute("view 1", model);

            assertTrue(displayed.isSamePerson(model.getViewedPerson().get()), command);
            execute("delete 1", model);
            assertNull(model.getViewedPerson().get(), command);
            assertTrue(model.hasPerson(ALICE), command);
        }
    }

    @Test
    public void execute_deleteAfterFiltering_deletesDisplayedContactNotHiddenSelection() throws Exception {
        for (String command : BENSON_FILTERS) {
            Model model = createModel(true);
            execute("view 1", model);
            Person viewed = model.getViewedPerson().get();
            execute(command, model);

            execute("delete 1", model);

            assertEquals(List.of(viewed), model.getAddressBook().getPersonList(), command);
            assertSame(viewed, model.getViewedPerson().get(), command);
            execute("list", model);
            execute("delete 1", model);
            assertNull(model.getViewedPerson().get(), command);
        }
    }

    private Model createModel(boolean isBensonStarred) {
        Person benson = new PersonBuilder(BENSON).withTags("work").withStarred(isBensonStarred).build();
        return new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(benson).build(),
                new UserPrefs(), FIXED_CLOCK);
    }

    private void execute(String command, Model model) throws Exception {
        new AddressBookParser().parseCommand(command).execute(model);
    }
}
