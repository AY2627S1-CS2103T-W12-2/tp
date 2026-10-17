package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUpDate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * The fixed clock says today is 2026-10-20 in Singapore.
 */
public class DueCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);
    }

    /** Sets {@code person}'s follow-up date (by its name) in the model. */
    private void setDate(Person person, String date) {
        Person current = model.getAddressBook().getPersonList().stream()
                .filter(p -> p.isSamePerson(person)).findFirst().get();
        model.setPerson(current, new PersonBuilder(current).withFollowUpDate(date).build());
    }

    private List<String> displayedNames() {
        return model.getFilteredPersonList().stream().map(p -> p.getName().fullName).collect(Collectors.toList());
    }

    @Test
    public void execute_someDue_listsDueOrderedByDate() throws Exception {
        setDate(ALICE, "2026-10-21"); // tomorrow
        setDate(BENSON, "2026-10-20"); // today
        setDate(CARL, "2026-10-18"); // overdue
        setDate(DANIEL, "2026-10-17"); // overdue, earliest

        CommandResult result = new DueCommand().execute(model);

        assertEquals(List.of(DANIEL.getName().fullName, CARL.getName().fullName, BENSON.getName().fullName),
                displayedNames());
        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, 3), result.getFeedbackToUser());
    }

    @Test
    public void execute_sameDate_keepsAddressBookOrder() throws Exception {
        setDate(DANIEL, "2026-10-18");
        setDate(CARL, "2026-10-18");

        new DueCommand().execute(model);

        // CARL comes before DANIEL in the address book
        assertEquals(List.of(CARL.getName().fullName, DANIEL.getName().fullName), displayedNames());
    }

    @Test
    public void execute_dueToday_included() throws Exception {
        setDate(BENSON, "2026-10-20");

        new DueCommand().execute(model);

        assertEquals(List.of(BENSON.getName().fullName), displayedNames());
    }

    @Test
    public void execute_dueTomorrow_excluded() throws Exception {
        setDate(BENSON, "2026-10-21");

        CommandResult result = new DueCommand().execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(DueCommand.MESSAGE_NONE_DUE, result.getFeedbackToUser());
    }

    @Test
    public void execute_noDates_showsNoneDueMessage() throws Exception {
        CommandResult result = new DueCommand().execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(DueCommand.MESSAGE_NONE_DUE, result.getFeedbackToUser());
    }

    @Test
    public void execute_oneDue_messageCountsContacts() throws Exception {
        setDate(ELLE, "2026-10-01");

        CommandResult result = new DueCommand().execute(model);

        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, 1), result.getFeedbackToUser());
    }

    @Test
    public void execute_afterFilter_searchesWholeAddressBook() throws Exception {
        setDate(CARL, "2026-10-18");
        showPersonAtIndex(model, INDEX_FIRST_PERSON); // only ALICE shown

        new DueCommand().execute(model);

        assertEquals(List.of(CARL.getName().fullName), displayedNames());
    }

    @Test
    public void execute_usesLocalDateWhereCommandRuns() throws Exception {
        // 2026-10-20 20:00 UTC is already 2026-10-21 04:00 in Singapore, so a contact due on the 21st is due
        Clock lateClock = Clock.fixed(Instant.parse("2026-10-20T20:00:00Z"), ZoneId.of("Asia/Singapore"));
        Model lateModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), lateClock);
        lateModel.setPerson(ALICE, new PersonBuilder(ALICE).withFollowUpDate("2026-10-21").build());

        new DueCommand().execute(lateModel);

        assertEquals(1, lateModel.getFilteredPersonList().size());
        assertEquals(ALICE.getName(), lateModel.getFilteredPersonList().get(0).getName());
    }

    @Test
    public void execute_doesNotChangeAddressBookOrActivity() throws Exception {
        setDate(CARL, "2026-10-18");
        AddressBook before = new AddressBook(model.getAddressBook());

        new DueCommand().execute(model);

        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_thenList_restoresAddressBookOrder() throws Exception {
        setDate(DANIEL, "2026-10-17");
        setDate(CARL, "2026-10-18");
        new DueCommand().execute(model);

        new ListCommand().execute(model);

        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_thenViewFirst_viewsFirstDueContact() throws Exception {
        setDate(CARL, "2026-10-18");
        setDate(DANIEL, "2026-10-17");
        new DueCommand().execute(model);

        new ViewCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(DANIEL.getName(), model.getViewedPerson().get().getName());
    }

    @Test
    public void execute_thenDeleteFirst_deletesFirstDueContact() throws Exception {
        setDate(CARL, "2026-10-18");
        setDate(DANIEL, "2026-10-17");
        new DueCommand().execute(model);

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertFalse(model.getAddressBook().getPersonList().stream().anyMatch(DANIEL::isSamePerson));
        assertTrue(model.getAddressBook().getPersonList().stream().anyMatch(CARL::isSamePerson));
    }

    @Test
    public void execute_thenClearDateOfFirst_updatesIntendedContact() throws Exception {
        setDate(CARL, "2026-10-18");
        setDate(DANIEL, "2026-10-17");
        new DueCommand().execute(model);

        new FollowUpCommand(INDEX_FIRST_PERSON, FollowUpDate.NONE).execute(model);

        Person daniel = model.getAddressBook().getPersonList().stream().filter(DANIEL::isSamePerson).findFirst().get();
        Person carl = model.getAddressBook().getPersonList().stream().filter(CARL::isSamePerson).findFirst().get();
        assertTrue(daniel.getFollowUpDate().isEmpty());
        assertEquals(new FollowUpDate("2026-10-18"), carl.getFollowUpDate());
    }

    @Test
    public void execute_thenEdit_keepsDateOfEditedContact() throws Exception {
        setDate(CARL, "2026-10-18");
        setDate(DANIEL, "2026-10-17");
        new DueCommand().execute(model);

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("99999999").build()).execute(model);

        Person daniel = model.getAddressBook().getPersonList().stream()
                .filter(p -> p.getName().equals(DANIEL.getName())).findFirst().get();
        // the contact that was first in the due list (Daniel) was edited, and kept its date
        assertEquals("99999999", daniel.getPhone().value);
        assertEquals(new FollowUpDate("2026-10-17"), daniel.getFollowUpDate());
    }

    @Test
    public void equals() {
        DueCommand command = new DueCommand();

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DueCommand()));
        assertFalse(command.equals(null));
        assertFalse(command.equals(5));
        assertFalse(command.equals(new ListCommand()));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DueCommand().execute(null));
    }
}
