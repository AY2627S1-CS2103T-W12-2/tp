package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.SortCriterion;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalTimes;

public class DueCommandTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DueCommand().execute(null));
    }

    @Test
    public void execute_showsDueAndOverdueEarliestFirst() throws Exception {
        Person none = person("None", "10000001", null);
        Person future = person("Future", "10000002", TODAY.plusDays(1));
        Person today = person("Today", "10000003", TODAY);
        Person overdue = person("Overdue", "10000004", TODAY.minusDays(5));
        Person older = person("Older", "10000005", TODAY.minusDays(40));
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, none, future, today, overdue, older);

        CommandResult result = new DueCommand().execute(model);

        assertEquals(List.of(older, overdue, today), model.getFilteredPersonList());
        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, TODAY, 3), result.getFeedbackToUser());
    }

    @Test
    public void execute_sameDate_keepsOriginalListOrder() throws Exception {
        Person second = person("Second", "10000001", TODAY);
        Person first = person("First", "10000002", TODAY);
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, second, first);

        new DueCommand().execute(model);

        assertEquals(List.of(second, first), model.getFilteredPersonList());
    }

    @Test
    public void execute_noDueContacts_isValidEmptyResult() throws Exception {
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, person("None", "10000001", null),
                person("Future", "10000002", TODAY.plusDays(3)));

        CommandResult result = new DueCommand().execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, TODAY, 0), result.getFeedbackToUser());
    }

    @Test
    public void execute_singaporeMidnightBoundary_decidesTodayBySingaporeDate() throws Exception {
        Person dueOnTwentyFirst = person("Boundary", "10000001", LocalDate.of(2026, 10, 21));
        // 23:59:59 on 20 Oct in Singapore is 15:59:59 UTC; one second later it is 21 Oct in Singapore
        Model beforeMidnight = modelWith(clockAt("2026-10-20T15:59:59Z"), dueOnTwentyFirst);
        Model atMidnight = modelWith(clockAt("2026-10-20T16:00:00Z"), dueOnTwentyFirst);

        new DueCommand().execute(beforeMidnight);
        new DueCommand().execute(atMidnight);

        assertTrue(beforeMidnight.getFilteredPersonList().isEmpty());
        assertEquals(List.of(dueOnTwentyFirst), atMidnight.getFilteredPersonList());
    }

    @Test
    public void execute_overridesActiveSort() throws Exception {
        Person late = person("Late", "10000001", TODAY);
        Person early = person("Early", "10000002", TODAY.minusDays(9));
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, late, early);
        model.sortFilteredPersonList(SortCriterion.ADDED);

        new DueCommand().execute(model);

        assertEquals(List.of(early, late), model.getFilteredPersonList());
    }

    @Test
    public void execute_laterSortReordersDueContacts() throws Exception {
        Person first = person("First", "10000001", TODAY.minusDays(1));
        Person second = person("Second", "10000002", TODAY.minusDays(2));
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, first, second);
        new DueCommand().execute(model);
        assertEquals(List.of(second, first), model.getFilteredPersonList());

        model.clearPersonListSort();

        assertEquals(List.of(first, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_indexesFollowDisplayedOrder() throws Exception {
        Person later = person("Later", "10000001", TODAY);
        Person earlier = person("Earlier", "10000002", TODAY.minusDays(7));
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, later, earlier);
        new DueCommand().execute(model);

        new FollowUpCommand(seedu.address.commons.core.index.Index.fromOneBased(1), null).execute(model);

        assertNull(model.getAddressBook().getPersonList().stream()
                .filter(p -> p.getName().fullName.equals("Earlier")).findFirst().get()
                .getFollowUpDate().orElse(null));
        assertEquals(List.of("Later"), model.getFilteredPersonList().stream()
                .map(p -> p.getName().fullName).toList());
    }

    @Test
    public void execute_keepsViewedContactThatIsNotDue() throws Exception {
        Person notDue = person("NotDue", "10000001", null);
        Person due = person("Due", "10000002", TODAY);
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, notDue, due);
        model.setViewedPerson(notDue);

        new DueCommand().execute(model);

        assertEquals(notDue, model.getViewedPerson().get());
        assertFalse(model.getFilteredPersonList().contains(notDue));
    }

    @Test
    public void execute_listResetsDueView() throws CommandException {
        Person none = person("None", "10000001", null);
        Person due = person("Due", "10000002", TODAY);
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, none, due);
        new DueCommand().execute(model);

        new ListCommand().execute(model);

        assertEquals(List.of(none, due), model.getFilteredPersonList());
    }

    @Test
    public void execute_viewedContactThatIsDue_keepsSelection() throws Exception {
        Person due = person("Due", "10000001", TODAY);
        Person other = person("Other", "10000002", null);
        Model model = modelWith(TypicalTimes.FIXED_CLOCK, due, other);
        model.setViewedPerson(due);

        new DueCommand().execute(model);

        assertEquals(due, model.getViewedPerson().get());
    }

    @Test
    public void equals() {
        DueCommand command = new DueCommand();
        assertEquals(command, command);
        assertEquals(new DueCommand(), new DueCommand());
        assertEquals(new DueCommand().hashCode(), new DueCommand().hashCode());
        org.junit.jupiter.api.Assertions.assertNotEquals(new DueCommand(), new ListCommand());
    }

    private static Clock clockAt(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Singapore"));
    }

    private static Person person(String name, String phone, LocalDate followUp) {
        return new PersonBuilder().withName(name).withPhone(phone).withEmail(name.toLowerCase() + "@example.com")
                .withFollowUpDate(followUp).build();
    }

    private static Model modelWith(Clock clock, Person... persons) {
        AddressBook book = new AddressBook();
        for (Person person : persons) {
            book.addPerson(person);
        }
        return new ModelManager(book, new UserPrefs(), clock);
    }
}
