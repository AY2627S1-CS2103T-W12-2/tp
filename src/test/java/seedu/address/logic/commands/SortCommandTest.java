package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalTimes.EARLIER;
import static seedu.address.testutil.TypicalTimes.EARLIEST;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.SortCriterion;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests for {@code SortCommand}.
 */
public class SortCommandTest {

    @Test
    public void constructor_nullCriterion_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(SortCriterion.ADDED).execute(null));
    }

    @Test
    public void equals_sameCriterion_returnsTrue() {
        SortCommand command = new SortCommand(SortCriterion.ADDED);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new SortCommand(SortCriterion.ADDED)));
        assertFalse(command.equals(new SortCommand(SortCriterion.EDITED)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("added"));
        assertEquals(command.hashCode(), new SortCommand(SortCriterion.ADDED).hashCode());
    }

    @Test
    public void toString_containsSortCriterion() {
        assertTrue(new SortCommand(SortCriterion.ADDED).toString().contains("ADDED"));
    }

    @Test
    public void execute_sortAdded_ordersNewestFirstAndUnknownLast() throws Exception {
        Person oldest = person("Oldest", "11111111", new Activity(EARLIEST, null, null));
        Person unknown = person("Unknown", "22222222", Activity.EMPTY);
        Person newest = person("Newest", "33333333", new Activity(NOW, null, null));
        ModelManager model = modelWith(oldest, unknown, newest);

        CommandResult result = new SortCommand(SortCriterion.ADDED).execute(model);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, "added", 3), result.getFeedbackToUser());
        assertEquals(List.of(newest, oldest, unknown), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortEdited_keepsOriginalOrderForEqualTimes() throws Exception {
        Person first = person("First", "11111111", new Activity(null, EARLIER, null));
        Person second = person("Second", "22222222", new Activity(null, EARLIER, null));
        Person unknown = person("Unknown", "33333333", Activity.EMPTY);
        ModelManager model = modelWith(second, unknown, first);

        new SortCommand(SortCriterion.EDITED).execute(model);

        assertEquals(List.of(second, first, unknown), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortEdited_keepsEqualTimeOrderAfterStarChange() throws Exception {
        Person first = person("First", "11111111", new Activity(null, NOW, null));
        Person second = person("Second", "22222222", new Activity(null, NOW, null));
        ModelManager model = modelWith(FIXED_CLOCK, first, second);

        new SortCommand(SortCriterion.EDITED).execute(model);
        model.editPerson(first, first.toBuilder().withStarred(true).build());

        assertEquals(List.of("First", "Second"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void execute_sortAdded_keepsEqualTimeOrderAfterAdd() throws Exception {
        Person first = person("First", "11111111", new Activity(NOW, null, null));
        Person second = person("Second", "22222222", new Activity(NOW, null, null));
        ModelManager model = modelWith(first);

        new SortCommand(SortCriterion.ADDED).execute(model);
        model.addPerson(second);

        assertEquals(List.of(first, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortViewed_reordersAfterSuccessfulView() throws Exception {
        Person viewedEarlier = person("Earlier", "11111111", new Activity(null, null, EARLIER));
        Person notViewed = person("Not Viewed", "22222222", Activity.EMPTY);
        ModelManager model = modelWith(FIXED_CLOCK, viewedEarlier, notViewed);

        new SortCommand(SortCriterion.VIEWED).execute(model);
        new ViewCommand(Index.fromOneBased(2)).execute(model);

        assertEquals("Not Viewed", model.getFilteredPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_sortingFilteredList_keepsOnlyMatchingContacts() throws Exception {
        Person olderMatch = person("Match Older", "11111111", new Activity(EARLIEST, null, null));
        Person excluded = person("Excluded", "22222222", new Activity(NOW, null, null));
        Person newerMatch = person("Match Newer", "33333333", new Activity(EARLIER, null, null));
        ModelManager model = modelWith(olderMatch, excluded, newerMatch);
        model.updateFilteredPersonList(person -> person.getName().fullName.startsWith("Match"));

        new SortCommand(SortCriterion.ADDED).execute(model);

        assertEquals(List.of(newerMatch, olderMatch), model.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterSort_restoresOriginalOrder() throws Exception {
        Person oldest = person("Oldest", "11111111", new Activity(EARLIEST, null, null));
        Person newest = person("Newest", "22222222", new Activity(NOW, null, null));
        ModelManager model = modelWith(oldest, newest);

        new SortCommand(SortCriterion.ADDED).execute(model);
        new ListCommand().execute(model);

        assertEquals(List.of(oldest, newest), model.getFilteredPersonList());
    }

    @Test
    public void execute_resetAfterSort_restoresOriginalOrder() throws Exception {
        Person oldest = person("Oldest", "11111111", new Activity(EARLIEST, null, null));
        Person newest = person("Newest", "22222222", new Activity(NOW, null, null));
        ModelManager model = modelWith(oldest, newest);

        new SortCommand(SortCriterion.ADDED).execute(model);
        CommandResult result = new ResetCommand().execute(model);

        assertEquals(ResetCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(oldest, newest), model.getFilteredPersonList());
    }

    private static ModelManager modelWith(Person... persons) {
        return modelWith(null, persons);
    }

    private static ModelManager modelWith(java.time.Clock clock, Person... persons) {
        AddressBook addressBook = new AddressBook();
        for (Person person : persons) {
            addressBook.addPerson(person);
        }
        return clock == null
                ? new ModelManager(addressBook, new UserPrefs())
                : new ModelManager(addressBook, new UserPrefs(), clock);
    }

    private static Person person(String name, String phone, Activity activity) {
        return new PersonBuilder().withName(name).withPhone(phone).withActivity(activity).build();
    }
}
