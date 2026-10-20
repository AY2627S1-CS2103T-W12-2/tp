package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class StarCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);

    @Test
    public void execute_starUnstarredPerson_success() {
        Model expectedModel = copyOf(model);
        expectedModel.editPerson(ALICE, ALICE.toBuilder().withStarred(true).build());

        String expectedMessage = String.format(StarCommand.MESSAGE_STAR_SUCCESS, ALICE.getName().fullName);

        assertCommandSuccess(new StarCommand(INDEX_FIRST_PERSON, true), model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList().get(0).isStarred());
    }

    @Test
    public void execute_unstarStarredPerson_success() {
        Model starredModel = starredFirstPersonModel();
        Person starred = starredModel.getFilteredPersonList().get(0);
        Model expectedModel = copyOf(starredModel);
        expectedModel.editPerson(starred, starred.toBuilder().withStarred(false).build());

        String expectedMessage = String.format(StarCommand.MESSAGE_UNSTAR_SUCCESS, ALICE.getName().fullName);

        assertCommandSuccess(new StarCommand(INDEX_FIRST_PERSON, false), starredModel, expectedMessage,
                expectedModel);
        assertFalse(starredModel.getFilteredPersonList().get(0).isStarred());
    }

    @Test
    public void execute_starAlreadyStarredPerson_leavesPersonUnchanged() {
        Model starredModel = starredFirstPersonModel();
        Person before = starredModel.getFilteredPersonList().get(0);

        String expectedMessage = String.format(StarCommand.MESSAGE_ALREADY_STARRED, ALICE.getName().fullName);

        assertCommandSuccess(new StarCommand(INDEX_FIRST_PERSON, true), starredModel, expectedMessage,
                copyOf(starredModel));
        assertSame(before, starredModel.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_unstarPersonThatIsNotStarred_leavesPersonUnchanged() {
        String expectedMessage = String.format(StarCommand.MESSAGE_NOT_STARRED, ALICE.getName().fullName);

        assertCommandSuccess(new StarCommand(INDEX_FIRST_PERSON, false), model, expectedMessage, copyOf(model));
        assertSame(ALICE, model.getFilteredPersonList().get(0));
    }

    /*
     * Index partitions: within the list (tested above), beyond the list (boundary: size + 1), empty list.
     */
    @Test
    public void execute_indexBeyondList_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new StarCommand(outOfBounds, true), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);

        assertCommandFailure(new StarCommand(INDEX_FIRST_PERSON, true), emptyModel,
                Messages.MESSAGE_EMPTY_PERSON_LIST);
    }

    @Test
    public void equals() {
        StarCommand star = new StarCommand(INDEX_FIRST_PERSON, true);

        assertEquals(star, star);
        assertEquals(star, new StarCommand(INDEX_FIRST_PERSON, true));
        assertNotEquals(star, new StarCommand(INDEX_FIRST_PERSON, false));
        assertNotEquals(star, new StarCommand(Index.fromOneBased(2), true));
        assertNotEquals(star, null);
        assertNotEquals(star, new ClearCommand());
    }

    @Test
    public void toStringMethod() {
        String expected = StarCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", starred=true}";
        assertEquals(expected, new StarCommand(INDEX_FIRST_PERSON, true).toString());
    }

    private static Model copyOf(Model source) {
        return new ModelManager(new AddressBook(source.getAddressBook()), new UserPrefs(), FIXED_CLOCK);
    }

    /** Returns a model like the typical one, but where the first person (ALICE) is starred. */
    private static Model starredFirstPersonModel() {
        AddressBook addressBook = getTypicalAddressBook();
        addressBook.setPerson(ALICE, ALICE.toBuilder().withStarred(true).build());
        return new ModelManager(addressBook, new UserPrefs(), FIXED_CLOCK);
    }
}
