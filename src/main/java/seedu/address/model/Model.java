package seedu.address.model;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** The time zone used to display activity times and to decide what today is. */
    ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Singapore");

    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * An opaque copy of the parts of the model that a command can change: the contacts, the viewed contact and
     * the visible-list filter. Created with {@link #createSnapshot()} and given back to {@link #restore(Snapshot)}.
     */
    interface Snapshot {}

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Returns a snapshot of the contacts, the viewed contact and the visible-list filter as they are now.
     * Later changes to the model do not change the snapshot.
     */
    Snapshot createSnapshot();

    /**
     * Puts the contacts, the viewed contact and the visible-list filter back to what they were when
     * {@code snapshot} was created. Used when a command's change could not be saved.
     * {@code snapshot} must come from {@link #createSnapshot()} of this model.
     */
    void restore(Snapshot snapshot);

    /**
     * Returns the clock used to record when things happen.
     */
    Clock getClock();

    /**
     * Returns the current time from {@link #getClock()}, in whole seconds.
     */
    default Instant now() {
        return Instant.now(getClock()).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Replaces {@code target} with {@code editedPerson} as a user edit, and records the edit time.
     * This is the one place that decides when "last edited" changes, so every command that changes a
     * contact should use it instead of {@link #setPerson(Person, Person)}:
     * <ul>
     * <li>If {@code editedPerson} has the same details as {@code target} (see
     * {@link Person#hasSameDetails(Person)}), nothing happens and no time changes.</li>
     * <li>Otherwise the stored person gets {@code target}'s activity with last edited set to now. The activity of
     * {@code editedPerson} is ignored, so a copy built without activity cannot wipe the other times.</li>
     * </ul>
     * {@code target} must exist in the address book. The same identity rule as {@code setPerson} applies.
     */
    void editPerson(Person target, Person editedPerson);

    /**
     * Records that {@code person} was viewed (sets last viewed to now) and makes it the viewed person.
     * {@code person} must exist in the address book.
     */
    void viewPerson(Person person);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Returns the person currently being viewed in the detail panel.
     * The value is null if no person is being viewed.
     */
    ReadOnlyObjectProperty<Person> getViewedPerson();

    /**
     * Sets the person to be viewed in the detail panel, without recording a view.
     * A null {@code person} means that no person is being viewed.
     * To handle a user's {@code view} command use {@link #viewPerson(Person)}, which also sets the last viewed time.
     */
    void setViewedPerson(Person person);
}
