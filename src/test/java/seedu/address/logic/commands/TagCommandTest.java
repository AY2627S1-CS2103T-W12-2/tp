package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

public class TagCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addsTagsWithoutRemovingExistingTags_success() {
        Set<Tag> updatedTags = new HashSet<>(ALICE.getTags());
        updatedTags.add(new Tag("work"));
        Person updatedPerson = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getLinkedIn(),
                updatedTags);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(ALICE, updatedPerson);

        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("work")));
        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(updatedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addsTagsWithoutRemovingLinkedIn_success() {
        Set<Tag> updatedTags = new HashSet<>(BENSON.getTags());
        updatedTags.add(new Tag("work"));
        Person updatedPerson = new Person(BENSON.getName(), BENSON.getPhone(), BENSON.getEmail(),
                BENSON.getLinkedIn(), updatedTags);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON, updatedPerson);

        TagCommand command = new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("work")));
        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(updatedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_existingTag_doesNotReplacePerson() {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("friends")));
        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(ALICE));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertSame(ALICE, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertEquals(Set.of(new Tag("friends")),
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getTags());
    }

    @Test
    public void execute_duplicateTagsIgnoringCase_addsSingleLowercaseTag() {
        Set<Tag> tagsToAdd = new HashSet<>(List.of(new Tag("Java"), new Tag("java")));
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, tagsToAdd);
        Set<Tag> updatedTags = new HashSet<>(ALICE.getTags());
        updatedTags.add(new Tag("java"));
        Person updatedPerson = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getLinkedIn(), updatedTags);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(ALICE, updatedPerson);
        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(updatedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(updatedTags, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getTags());
    }

    @Test
    public void execute_validIndexFilteredList_addsTagToDisplayedPerson() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Set<Tag> updatedTags = new HashSet<>(BENSON.getTags());
        updatedTags.add(new Tag("work"));
        Person updatedPerson = new Person(BENSON.getName(), BENSON.getPhone(), BENSON.getEmail(),
                BENSON.getLinkedIn(), updatedTags);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(BENSON, updatedPerson);

        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("work")));
        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(updatedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        TagCommand command = new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("work")));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        TagCommand command = new TagCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                Set.of(new Tag("work")));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        TagCommand firstCommand = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("work")));
        TagCommand firstCommandCopy = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("work")));
        TagCommand secondCommand = new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("work")));
        TagCommand thirdCommand = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("friends")));

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(firstCommandCopy));
        assertFalse(firstCommand.equals(secondCommand));
        assertFalse(firstCommand.equals(thirdCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(new ListCommand()));
        assertEquals(firstCommand.hashCode(), firstCommandCopy.hashCode());
    }

    @Test
    public void toStringMethod() {
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("work")));
        String expected = TagCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", tagsToAdd=[[work]]}";

        assertEquals(expected, command.toString());
    }
}
