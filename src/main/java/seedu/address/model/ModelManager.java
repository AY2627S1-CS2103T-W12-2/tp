package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private static final int MAX_HISTORY_SIZE = 50;
    private static final String NOTHING_TO_UNDO = "Nothing to undo.";
    private static final String NOTHING_TO_REDO = "Nothing to redo.";

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>();
    private final ReadOnlyStringWrapper personListStatus = new ReadOnlyStringWrapper();
    private final Clock clock;
    private SortCriterion sortCriterion;
    private String filterDescription = "";
    private final Deque<State> undoHistory = new ArrayDeque<>();
    private final Deque<State> redoHistory = new ArrayDeque<>();

    private static class State {
        private final AddressBook addressBook;
        private final Person viewedPerson;
        private final Predicate<? super Person> filter;
        private final SortCriterion sortCriterion;
        private final String filterDescription;

        State(ReadOnlyAddressBook addressBook, Person viewedPerson, Predicate<? super Person> filter,
                SortCriterion sortCriterion, String filterDescription) {
            this.addressBook = new AddressBook(addressBook);
            this.viewedPerson = viewedPerson;
            this.filter = filter;
            this.sortCriterion = sortCriterion;
            this.filterDescription = filterDescription;
        }
    }

    private static class ModelSnapshot implements Snapshot {
        private final State state;
        private final Deque<State> undoHistory;
        private final Deque<State> redoHistory;

        ModelSnapshot(State state, Deque<State> undoHistory, Deque<State> redoHistory) {
            this.state = state;
            this.undoHistory = new ArrayDeque<>(undoHistory);
            this.redoHistory = new ArrayDeque<>(redoHistory);
        }
    }

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, Clock.system(DISPLAY_ZONE));
    }

    /**
     * Initializes a ModelManager with the given addressBook, userPrefs and the clock used to record activity.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, Clock clock) {
        requireAllNonNull(addressBook, userPrefs, clock);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
        sortedPersons.addListener((ListChangeListener<Person>) change -> updatePersonListStatus());
        this.clock = clock;
        updatePersonListStatus();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        viewedPerson.set(null);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
        if (target.equals(viewedPerson.get())) {
            viewedPerson.set(null);
        }
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
        if (target.equals(viewedPerson.get())) {
            viewedPerson.set(editedPerson);
        }
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public void editPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        // checked first so that a no-op edit of a missing or stale target fails like any other edit
        if (!addressBook.getPersonList().contains(target)) {
            throw new PersonNotFoundException();
        }
        if (editedPerson.hasSameDetails(target)) {
            return;
        }
        setPerson(target, editedPerson.withActivity(target.getActivity().withLastEditedAt(now())));
    }

    @Override
    public void viewPerson(Person person) {
        requireNonNull(person);

        Person viewed = person.withActivity(person.getActivity().withLastViewedAt(now()));
        addressBook.setPerson(person, viewed);
        viewedPerson.set(viewed);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        updateFilteredPersonList(predicate, predicate == PREDICATE_SHOW_ALL_PERSONS ? "" : "Filtered contacts");
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, String filterDescription) {
        requireAllNonNull(predicate, filterDescription);
        this.filterDescription = filterDescription;
        filteredPersons.setPredicate(predicate);
        updatePersonListStatus();
    }

    @Override
    public void sortFilteredPersonList(SortCriterion sortCriterion) {
        setSortCriterion(requireNonNull(sortCriterion));
    }

    @Override
    public void clearPersonListSort() {
        setSortCriterion(null);
    }

    @Override
    public ReadOnlyStringProperty getPersonListStatus() {
        return personListStatus.getReadOnlyProperty();
    }

    private void updatePersonListStatus() {
        String orderDescription = sortCriterion == null
                ? "Original order"
                : "Sorted by " + sortCriterion.getCommandArgument();
        String filterPrefix = filterDescription.isEmpty() ? "" : filterDescription + " - ";
        personListStatus.set(String.format("%s%s - %d contact(s)", filterPrefix,
                orderDescription, sortedPersons.size()));
    }

    //=========== Viewed Person Accessors ====================================================================

    @Override
    public ReadOnlyObjectProperty<Person> getViewedPerson() {
        return viewedPerson;
    }

    @Override
    public void setViewedPerson(Person person) {
        viewedPerson.set(person);
    }

    @Override
    public Snapshot createSnapshot() {
        return new ModelSnapshot(captureState(), undoHistory, redoHistory);
    }

    @Override
    public void restore(Snapshot snapshot) {
        ModelSnapshot saved = (ModelSnapshot) requireNonNull(snapshot);
        restoreState(saved.state, false);
        undoHistory.clear();
        undoHistory.addAll(saved.undoHistory);
        redoHistory.clear();
        redoHistory.addAll(saved.redoHistory);
    }

    @Override
    public void recordSuccessfulMutation(Snapshot before) {
        ModelSnapshot saved = (ModelSnapshot) requireNonNull(before);
        if (hasContactChange(saved.state.addressBook, addressBook)) {
            undoHistory.addLast(saved.state);
            if (undoHistory.size() > MAX_HISTORY_SIZE) {
                undoHistory.removeFirst();
            }
            redoHistory.clear();
        }
    }

    @Override
    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    @Override
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException(NOTHING_TO_UNDO);
        }
        State current = captureState();
        State previous = undoHistory.removeLast();
        redoHistory.addLast(current);
        restoreState(previous, true);
    }

    @Override
    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    @Override
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException(NOTHING_TO_REDO);
        }
        State current = captureState();
        State next = redoHistory.removeLast();
        undoHistory.addLast(current);
        restoreState(next, true);
    }

    private State captureState() {
        return new State(addressBook, viewedPerson.get(), filteredPersons.getPredicate(), sortCriterion,
                filterDescription);
    }

    private static boolean hasContactChange(ReadOnlyAddressBook before, ReadOnlyAddressBook after) {
        return !withoutViewTimes(before).equals(withoutViewTimes(after));
    }

    private static Set<Person> withoutViewTimes(ReadOnlyAddressBook book) {
        Set<Person> persons = new HashSet<>();
        for (Person person : book.getPersonList()) {
            Activity activity = person.getActivity();
            persons.add(person.withActivity(new Activity(activity.getCreatedAt().orElse(null),
                    activity.getLastEditedAt().orElse(null), null)));
        }
        return persons;
    }

    private void restoreState(State state, boolean keepLaterViews) {
        List<Person> restored = new ArrayList<>();
        List<Person> savedPersons = state.addressBook.getPersonList();
        for (int i = 0; i < savedPersons.size(); i++) {
            Person saved = savedPersons.get(i);
            if (keepLaterViews) {
                saved = preserveRecentViewTime(saved, findCurrentPerson(saved, i, savedPersons));
            }
            restored.add(saved);
        }
        addressBook.setPersons(restored);
        filterDescription = state.filterDescription;
        filteredPersons.setPredicate(state.filter);
        setSortCriterion(state.sortCriterion);
        viewedPerson.set(findRestoredSelection(state.viewedPerson, restored));
    }

    private void setSortCriterion(SortCriterion sortCriterion) {
        this.sortCriterion = sortCriterion;
        Comparator<Person> comparator = null;
        if (sortCriterion != null) {
            comparator = sortCriterion.getComparator()
                    .thenComparingInt(addressBook.getPersonList()::indexOf);
        }
        sortedPersons.setComparator(comparator);
        updatePersonListStatus();
    }

    private static Person preserveRecentViewTime(Person saved, Person current) {
        if (current == null) {
            return saved;
        }
        Instant savedView = saved.getActivity().getLastViewedAt().orElse(null);
        Instant currentView = current.getActivity().getLastViewedAt().orElse(null);
        if (currentView != null && (savedView == null || currentView.isAfter(savedView))) {
            return saved.withActivity(saved.getActivity().withLastViewedAt(currentView));
        }
        return saved;
    }

    private Person findCurrentPerson(Person saved, int index, List<Person> savedPersons) {
        for (Person current : addressBook.getPersonList()) {
            if (saved.isSamePerson(current)) {
                return current;
            }
        }
        // A real edit can change one contact's identity while all other contacts keep their positions.
        if (hasSingleIdentityChangeAt(index, savedPersons)) {
            Person current = addressBook.getPersonList().get(index);
            // Undo can restore an identity edit made in the same second as the previous edit.
            // Redo can restore it when the earlier version has no last-edited time.
            if (current.getActivity().getLastEditedAt().isPresent()
                    || !saved.getActivity().getLastEditedAt().equals(current.getActivity().getLastEditedAt())) {
                return current;
            }
        }
        return null;
    }

    private boolean hasSingleIdentityChangeAt(int index, List<Person> savedPersons) {
        List<Person> currentPersons = addressBook.getPersonList();
        if (savedPersons.size() != currentPersons.size()) {
            return false;
        }
        for (int i = 0; i < savedPersons.size(); i++) {
            if (i != index && !savedPersons.get(i).isSamePerson(currentPersons.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static Person findRestoredSelection(Person selected, List<Person> restored) {
        if (selected == null) {
            return null;
        }
        for (Person person : restored) {
            if (selected.isSamePerson(person)) {
                return person;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && sortCriterion == otherModelManager.sortCriterion
                && filterDescription.equals(otherModelManager.filterDescription);
    }

}
