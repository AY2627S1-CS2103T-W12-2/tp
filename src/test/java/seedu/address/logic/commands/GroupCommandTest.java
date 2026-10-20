package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.TagMatchesPredicate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class GroupCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_matchingTag_listsMatchingPersonsWithGroupLabelAndCount() {
        Tag tag = new Tag("FRIENDS");
        TagMatchesPredicate predicate = new TagMatchesPredicate(tag);
        GroupCommand command = new GroupCommand(tag);
        String expectedMessage = String.format(GroupCommand.MESSAGE_SUCCESS, tag, 3);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatchingTag_listsNoPersonsWithZeroCount() {
        Tag tag = new Tag("react");
        GroupCommand command = new GroupCommand(tag);
        String expectedMessage = String.format(GroupCommand.MESSAGE_SUCCESS, tag, 0);
        expectedModel.updateFilteredPersonList(new TagMatchesPredicate(tag));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void constructor_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupCommand(new Tag("react")).execute(null));
    }

    @Test
    public void equals_sameTag_returnsTrue() {
        GroupCommand command = new GroupCommand(new Tag("react"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new GroupCommand(new Tag("REACT"))));
        assertFalse(command.equals(new GroupCommand(new Tag("figma"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("react"));
        assertEquals(command.hashCode(), new GroupCommand(new Tag("react")).hashCode());
    }

    @Test
    public void toString_containsTag() {
        assertTrue(new GroupCommand(new Tag("react")).toString().contains("react"));
    }

    @Test
    public void execute_listAfterGrouping_restoresFullList() throws Exception {
        new GroupCommand(new Tag("friends")).execute(model);

        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(getTypicalAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_groupedList_viewUsesGroupedIndex() throws Exception {
        groupByFriends(model);

        new ViewCommand(INDEX_SECOND_PERSON).execute(model);

        assertTrue(BENSON.isSamePerson(model.getViewedPerson().get()));
    }

    @Test
    public void execute_groupedList_editUsesGroupedIndex() throws Exception {
        groupByFriends(model);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail("grouped@example.com").build();

        new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        assertEquals("grouped@example.com", model.getAddressBook().getPersonList().get(1).getEmail().value);
    }

    @Test
    public void execute_groupedList_deleteUsesGroupedIndex() throws Exception {
        groupByFriends(model);

        new DeleteCommand(INDEX_SECOND_PERSON).execute(model);

        assertFalse(model.getAddressBook().getPersonList().contains(BENSON));
    }

    @Test
    public void execute_groupedList_tagUsesGroupedIndex() throws Exception {
        groupByFriends(model);

        new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("work"))).execute(model);

        assertEquals(Set.of(new Tag("friends"), new Tag("owesMoney"), new Tag("work")),
                model.getAddressBook().getPersonList().get(1).getTags());
    }

    @Test
    public void execute_groupExcludesViewedPerson_clearsSelection() throws Exception {
        new ViewCommand(INDEX_SECOND_PERSON).execute(model);

        new GroupCommand(new Tag("react")).execute(model);

        assertNull(model.getViewedPerson().get());
    }

    @Test
    public void execute_groupIncludesViewedPerson_keepsSelection() throws Exception {
        new ViewCommand(INDEX_SECOND_PERSON).execute(model);

        new GroupCommand(new Tag("friends")).execute(model);

        assertTrue(BENSON.isSamePerson(model.getViewedPerson().get()));
    }

    private void groupByFriends(Model targetModel) throws Exception {
        new GroupCommand(new Tag("friends")).execute(targetModel);
    }
}
