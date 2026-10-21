package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalTimes.EARLIER;
import static seedu.address.testutil.TypicalTimes.EARLIEST;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Activity;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    //=========== snapshot ===================================================================================

    @Test
    public void restore_afterChanges_bringsBackAddressBookViewedPersonAndFilter() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.viewPerson(modelManager.getFilteredPersonList().get(0));
        modelManager.updateFilteredPersonList(person -> person.getName().equals(BENSON.getName()));
        AddressBook addressBookBefore = new AddressBook(modelManager.getAddressBook());
        List<Person> visibleBefore = List.copyOf(modelManager.getFilteredPersonList());
        Person viewedBefore = modelManager.getViewedPerson().get();
        Model.Snapshot snapshot = modelManager.createSnapshot();

        modelManager.deletePerson(modelManager.getAddressBook().getPersonList().get(0));
        modelManager.addPerson(CARL);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        modelManager.setViewedPerson(null);
        modelManager.restore(snapshot);

        assertEquals(addressBookBefore, modelManager.getAddressBook());
        assertEquals(visibleBefore, modelManager.getFilteredPersonList());
        assertEquals(viewedBefore, modelManager.getViewedPerson().get());
    }

    @Test
    public void restore_noViewedPerson_keepsNoViewedPerson() {
        modelManager.addPerson(ALICE);
        Model.Snapshot snapshot = modelManager.createSnapshot();

        modelManager.viewPerson(ALICE);
        modelManager.restore(snapshot);

        assertNull(modelManager.getViewedPerson().get());
        assertEquals(Activity.EMPTY, modelManager.getAddressBook().getPersonList().get(0).getActivity());
    }

    @Test
    public void createSnapshot_laterChanges_doNotAffectSnapshot() {
        modelManager.addPerson(ALICE);
        Model.Snapshot snapshot = modelManager.createSnapshot();

        modelManager.addPerson(BENSON);
        modelManager.deletePerson(ALICE);
        modelManager.restore(snapshot);

        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        // restoring again after more changes gives the same state
        modelManager.addPerson(CARL);
        modelManager.restore(snapshot);
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void restore_doesNotChangeClockOrUserPrefs() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Model.Snapshot snapshot = timedModel.createSnapshot();
        timedModel.setGuiSettings(new GuiSettings(1, 2, 3, 4));

        timedModel.restore(snapshot);

        assertEquals(new GuiSettings(1, 2, 3, 4), timedModel.getGuiSettings());
        assertEquals(FIXED_CLOCK, timedModel.getClock());
    }

    @Test
    public void restore_nullSnapshot_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.restore(null));
    }

    //=========== activity ===================================================================================

    @Test
    public void getClock_default_isSingaporeClock() {
        assertEquals(ZoneId.of("Asia/Singapore"), Model.DISPLAY_ZONE);
        assertEquals(Model.DISPLAY_ZONE, modelManager.getClock().getZone());
    }

    @Test
    public void now_defaultClock_isCloseToSystemTime() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant now = modelManager.now();
        Instant after = Instant.now();

        assertFalse(now.isBefore(before));
        assertFalse(now.isAfter(after));
    }

    @Test
    public void now_fixedClock_dropsSubSecondPrecision() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        assertEquals(FIXED_CLOCK, timedModel.getClock());
        assertEquals(NOW, timedModel.now());
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ModelManager(new AddressBook(), new UserPrefs(), null));
    }

    @Test
    public void editPerson_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.editPerson(null, ALICE));
        assertThrows(NullPointerException.class, () -> modelManager.editPerson(ALICE, null));
    }

    @Test
    public void editPerson_changedDetails_setsLastEditedAndKeepsOtherTimes() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Person original = new PersonBuilder(ALICE)
                .withActivity(new Activity(EARLIEST, EARLIER, EARLIER)).build();
        timedModel.addPerson(original);

        timedModel.editPerson(original, new PersonBuilder(original).withName("Alice Pauline Tan").build());

        Person stored = timedModel.getAddressBook().getPersonList().get(0);
        assertEquals("Alice Pauline Tan", stored.getName().fullName);
        assertEquals(new Activity(EARLIEST, NOW, EARLIER), stored.getActivity());
    }

    @Test
    public void editPerson_personWithNoRecordedActivity_setsOnlyLastEdited() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        timedModel.addPerson(ALICE);

        timedModel.editPerson(ALICE, new PersonBuilder(ALICE).withTags("work").build());

        assertEquals(new Activity(null, NOW, null), timedModel.getAddressBook().getPersonList().get(0).getActivity());
    }

    @Test
    public void editPerson_identicalDetailsButTargetMissing_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> modelManager.editPerson(ALICE, ALICE));
    }

    @Test
    public void editPerson_identicalDetails_isNoOp() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Person original = new PersonBuilder(ALICE).withActivity(new Activity(EARLIEST, EARLIER, null)).build();
        timedModel.addPerson(original);

        timedModel.editPerson(original, new PersonBuilder(original).build());

        assertEquals(original, timedModel.getAddressBook().getPersonList().get(0));
        assertEquals(new Activity(EARLIEST, EARLIER, null),
                timedModel.getAddressBook().getPersonList().get(0).getActivity());
    }

    @Test
    public void editPerson_editedHasDifferentActivity_usesTargetActivity() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Person original = new PersonBuilder(ALICE).withActivity(new Activity(EARLIEST, null, EARLIER)).build();
        timedModel.addPerson(original);

        // an edited copy built without activity must not wipe the created and viewed times
        timedModel.editPerson(original, new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getLinkedIn(), Set.of(new Tag("work"))));

        assertEquals(new Activity(EARLIEST, NOW, EARLIER),
                timedModel.getAddressBook().getPersonList().get(0).getActivity());
    }

    @Test
    public void editPerson_viewedPerson_updatesViewedPersonWithStampedCopy() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        timedModel.addPerson(ALICE);
        timedModel.setViewedPerson(ALICE);

        timedModel.editPerson(ALICE, new PersonBuilder(ALICE).withTags("work").build());

        assertEquals(timedModel.getAddressBook().getPersonList().get(0), timedModel.getViewedPerson().get());
        assertEquals(NOW, timedModel.getViewedPerson().get().getActivity().getLastEditedAt().get());
    }

    @Test
    public void viewPerson_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.viewPerson(null));
    }

    @Test
    public void viewPerson_setsLastViewedAndViewedPerson() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Person original = new PersonBuilder(ALICE).withActivity(new Activity(EARLIEST, EARLIER, null)).build();
        timedModel.addPerson(original);

        timedModel.viewPerson(original);

        Person stored = timedModel.getAddressBook().getPersonList().get(0);
        assertEquals(new Activity(EARLIEST, EARLIER, NOW), stored.getActivity());
        assertEquals(stored, timedModel.getViewedPerson().get());
        assertTrue(stored.hasSameDetails(original));
    }

    @Test
    public void viewPerson_viewedAgain_replacesLastViewed() {
        ModelManager timedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        Person original = new PersonBuilder(ALICE).withActivity(new Activity(null, null, EARLIER)).build();
        timedModel.addPerson(original);

        timedModel.viewPerson(original);

        assertEquals(NOW, timedModel.getViewedPerson().get().getActivity().getLastViewedAt().get());
    }

    @Test
    public void getViewedPerson_noPersonViewed_returnsNull() {
        assertNull(modelManager.getViewedPerson().get());
    }

    @Test
    public void setViewedPerson_person_setsViewedPerson() {
        modelManager.setViewedPerson(ALICE);
        assertEquals(ALICE, modelManager.getViewedPerson().get());
    }

    @Test
    public void setViewedPerson_null_clearsViewedPerson() {
        modelManager.setViewedPerson(ALICE);
        modelManager.setViewedPerson(null);
        assertNull(modelManager.getViewedPerson().get());
    }

    @Test
    public void deletePerson_viewedPerson_clearsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        modelManager.deletePerson(ALICE);

        assertNull(modelManager.getViewedPerson().get());
    }

    @Test
    public void deletePerson_otherPerson_keepsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.deletePerson(BENSON);

        assertEquals(ALICE, modelManager.getViewedPerson().get());
    }

    @Test
    public void setPerson_viewedPerson_updatesViewedPerson() {
        Person editedAlice = new PersonBuilder(ALICE).withName("Alicia Pauline").build();
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        modelManager.setPerson(ALICE, editedAlice);

        assertEquals(editedAlice, modelManager.getViewedPerson().get());
    }

    @Test
    public void setPerson_otherPerson_keepsViewedPerson() {
        Person editedBenson = new PersonBuilder(BENSON).withName("Benny Meier").build();
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.setPerson(BENSON, editedBenson);

        assertEquals(ALICE, modelManager.getViewedPerson().get());
    }

    @Test
    public void setAddressBook_clearsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        modelManager.setAddressBook(new AddressBook());

        assertNull(modelManager.getViewedPerson().get());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void constructor_addressBookWithNote_doesNotLogTheNoteText() {
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(new PersonBuilder(ALICE).withNote("private text").build()).build();
        List<String> logged = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logged.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        // the constructor logs the whole address book at the most detailed level
        Logger logger = Logger.getLogger("ab3.ModelManager");
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);

        try {
            new ModelManager(addressBook, new UserPrefs());
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(oldLevel);
        }

        assertFalse(logged.isEmpty());
        assertTrue(logged.stream().anyMatch(message -> message.contains("[12 characters]")), logged.toString());
        assertTrue(logged.stream().noneMatch(message -> message.contains("private text")), logged.toString());
    }
}
