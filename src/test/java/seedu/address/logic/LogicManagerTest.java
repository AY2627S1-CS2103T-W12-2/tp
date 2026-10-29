package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_EMPTY_PERSON_LIST;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.parser.AddressBookParser.MESSAGE_UNKNOWN_COMMAND_WITH_LIST;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.GroupCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.FindCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_tabSeparatedAddAndEdit_targetTheContact() throws Exception {
        logic.execute("add\tn/Alice\tp/12345678");
        assertEquals("Alice", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("edit 1\tp/87654321");
        assertEquals("87654321", model.getFilteredPersonList().getFirst().getPhone().value);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND_WITH_LIST);
    }

    @Test
    public void execute_helpWithUnwritableStorage_returnsHelpWithoutSaving() throws Exception {
        useFailingStorageWithTypicalContacts();
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = logic.execute("help");

        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(before, model.getAddressBook());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_exitWithUnwritableStorage_returnsExitWithoutSaving() throws Exception {
        useFailingStorageWithTypicalContacts();
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = logic.execute("exit");

        assertTrue(result.isExit());
        assertFalse(result.isShowHelp());
        assertEquals(before, model.getAddressBook());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_helpAndExit_preserveInvalidDataFileUntilList() throws Exception {
        Path file = temporaryFolder.resolve("addressBook.json");
        String invalidJson = "{invalid json";
        Files.writeString(file, invalidJson);

        assertTrue(logic.execute("help").isShowHelp());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(invalidJson, Files.readString(file));

        logic.execute("list");
        assertTrue(new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_EMPTY_PERSON_LIST);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, LogicManager.MESSAGE_SAVE_ERROR);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, LogicManager.MESSAGE_SAVE_PERMISSION_ERROR);
    }

    @Test
    public void execute_secondCopyCannotOverwriteNewerContacts() throws Exception {
        Path file = temporaryFolder.resolve("shared-addressBook.json");
        Files.writeString(file, "{\"persons\":[]}");
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager firstStorage = new StorageManager(new JsonAddressBookStorage(file), prefs);
        StorageManager secondStorage = new StorageManager(new JsonAddressBookStorage(file), prefs);
        Model firstModel = new ModelManager(firstStorage.readAddressBook().orElseThrow(), new UserPrefs(), FIXED_CLOCK);
        Model secondModel = new ModelManager(secondStorage.readAddressBook().orElseThrow(),
                new UserPrefs(), FIXED_CLOCK);
        Logic firstLogic = new LogicManager(firstModel, firstStorage);
        Logic secondLogic = new LogicManager(secondModel, secondStorage);

        firstLogic.execute("add n/Alice");
        CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> secondLogic.execute("add n/Bob"));

        assertEquals("Contacts changed in another copy. Nothing changed. Reopen the app before trying again.",
                error.getMessage());
        assertEquals("Alice", new JsonAddressBookStorage(file).readAddressBook().orElseThrow()
                .getPersonList().getFirst().getName().fullName);
        assertTrue(secondModel.getAddressBook().getPersonList().isEmpty());
        assertFalse(secondModel.canUndo());
    }

    @Test
    public void execute_anotherCopyIsSaving_reportsBusyAndRollsBack() throws Exception {
        Path lockPath = temporaryFolder.resolve("addressBook.json.lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                    CommandException.class, () -> logic.execute("add n/Alice"));
            assertEquals("Another copy is saving contacts. Nothing changed. Try again.", error.getMessage());
        }
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndo());
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_failedSave_hidesTechnicalDetailsAndPreservesCause() {
        useFailingStorageWithTypicalContacts();
        CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> logic.execute("meeting 1 ev/Hackathon"));
        assertEquals(LogicManager.MESSAGE_SAVE_ERROR, error.getMessage());
        assertFalse(error.getMessage().contains(DUMMY_IO_EXCEPTION.getMessage()));
        assertEquals(DUMMY_IO_EXCEPTION, error.getCause());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getViewedPerson_noPersonViewed_returnsNull() {
        assertNull(logic.getViewedPerson().get());
    }

    @Test
    public void getViewedPerson_afterViewCommand_returnsViewedPerson() throws Exception {
        model.addPerson(AMY);

        logic.execute("view 1");

        assertTrue(AMY.hasSameDetails(logic.getViewedPerson().get()));
        assertEquals(new Activity(null, null, NOW), logic.getViewedPerson().get().getActivity());
    }

    @Test
    public void getViewedPerson_afterEditingViewedPerson_returnsEditedPerson() throws Exception {
        model.addPerson(AMY);
        logic.execute("view 1");

        logic.execute("edit 1 n/Bob Choo");

        Person editedPerson = logic.getFilteredPersonList().get(0);
        assertEquals("Bob Choo", editedPerson.getName().fullName);
        assertEquals(editedPerson, logic.getViewedPerson().get());
    }

    @Test
    public void getViewedPerson_afterTaggingViewedPerson_returnsTaggedPerson() throws Exception {
        model.addPerson(AMY);
        logic.execute("view 1");

        logic.execute("tag 1 t/hackathon");

        Person taggedPerson = logic.getFilteredPersonList().get(0);
        assertTrue(taggedPerson.getTags().contains(new Tag("hackathon")));
        assertEquals(taggedPerson, logic.getViewedPerson().get());
    }

    @Test
    public void getViewedPerson_afterDeletingViewedPerson_returnsNull() throws Exception {
        model.addPerson(AMY);
        logic.execute("view 1");

        logic.execute("delete 1");

        assertNull(logic.getViewedPerson().get());
    }

    @Test
    public void getViewedPerson_afterClear_returnsNull() throws Exception {
        model.addPerson(AMY);
        logic.execute("view 1");

        logic.execute("clear");

        assertNull(logic.getViewedPerson().get());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), FIXED_CLOCK);
        expectedModel.restore(model.createSnapshot());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY;
        // a failed save must not leave the new contact in memory
        ModelManager expectedModel = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addThenView_activitySurvivesSaveAndReload() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY);
        logic.execute("view 1");

        JsonAddressBookStorage reader = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Person reloaded = reader.readAddressBook().get().getPersonList().get(0);

        assertEquals(new Activity(NOW, null, NOW), reloaded.getActivity());
    }

    // ---- a failed save leaves the in-memory data as it was ----

    /** Replaces {@code logic} with one whose storage always fails to save, over the typical contacts. */
    private void useFailingStorageWithTypicalContacts() {
        for (Person person : getTypicalPersons()) {
            model.addPerson(person);
        }
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failing.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"));
        logic = new LogicManager(model, new StorageManager(failingStorage, userPrefsStorage));
    }

    private void assertFailedSaveLeavesModelUnchanged(String command) {
        useFailingStorageWithTypicalContacts();
        AddressBook addressBookBefore = new AddressBook(model.getAddressBook());
        List<Person> visibleBefore = new ArrayList<>(model.getFilteredPersonList());
        Person viewedBefore = model.getViewedPerson().get();

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_ERROR, () -> logic.execute(command));

        assertEquals(addressBookBefore, model.getAddressBook());
        assertEquals(visibleBefore, model.getFilteredPersonList());
        assertEquals(viewedBefore, model.getViewedPerson().get());
    }

    @Test
    public void execute_addWithFailedSave_leavesModelUnchanged() {
        assertFailedSaveLeavesModelUnchanged(AddCommand.COMMAND_WORD + " n/Zed Zoo p/99998888");
    }

    @Test
    public void execute_editWithFailedSave_leavesModelAndActivityUnchanged() {
        assertFailedSaveLeavesModelUnchanged("edit 1 p/91234567");
    }

    @Test
    public void execute_tagWithFailedSave_leavesModelUnchanged() {
        assertFailedSaveLeavesModelUnchanged("tag 1 t/newtag");
    }

    @Test
    public void execute_deleteWithFailedSave_leavesModelUnchanged() {
        assertFailedSaveLeavesModelUnchanged("delete 1");
    }

    @Test
    public void execute_clearWithFailedSave_leavesModelUnchanged() {
        assertFailedSaveLeavesModelUnchanged("clear");
    }

    @Test
    public void execute_viewWithFailedSave_doesNotRecordTheView() {
        assertFailedSaveLeavesModelUnchanged("view 1");
        assertEquals(Activity.EMPTY, model.getFilteredPersonList().get(0).getActivity());
    }

    @Test
    public void execute_viewWithFailedSaveAfterEarlierView_keepsEarlierViewedPerson() {
        useFailingStorageWithTypicalContacts();
        model.viewPerson(model.getFilteredPersonList().get(1));
        Person viewedBefore = model.getViewedPerson().get();
        AddressBook addressBookBefore = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, () -> logic.execute("view 3"));

        assertEquals(viewedBefore, model.getViewedPerson().get());
        assertEquals(addressBookBefore, model.getAddressBook());
    }

    @Test
    public void execute_findWithFailedSave_restoresPreviousVisibleList() {
        useFailingStorageWithTypicalContacts();
        List<Person> visibleBefore = new ArrayList<>(model.getFilteredPersonList());

        assertThrows(CommandException.class, () -> logic.execute("find Alice"));

        assertEquals(visibleBefore, model.getFilteredPersonList());
    }

    @Test
    public void execute_accessDeniedWithFailedSave_leavesModelUnchanged() {
        for (Person person : getTypicalPersons()) {
            model.addPerson(person);
        }
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("denied.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_AD_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("deniedPrefs.json"))));
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));

        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_successfulSave_keepsTheChange() throws Exception {
        for (Person person : getTypicalPersons()) {
            model.addPerson(person);
        }
        int sizeBefore = model.getAddressBook().getPersonList().size();

        logic.execute("delete 1");

        assertEquals(sizeBefore - 1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_noteCommand_isSavedAndSurvivesReload() throws Exception {
        model.addPerson(AMY);

        logic.execute("note 1 nt/Met at booth 4, wants a React teammate (Sat 10am)!");

        assertEquals("Met at booth 4, wants a React teammate (Sat 10am)!",
                logic.getFilteredPersonList().get(0).getNote().value);
        Person reloaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().get().getPersonList().get(0);
        assertEquals("Met at booth 4, wants a React teammate (Sat 10am)!", reloaded.getNote().value);

        logic.execute("note 1 clear");

        assertTrue(logic.getFilteredPersonList().get(0).getNote().isEmpty());
        reloaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().get().getPersonList().get(0);
        assertTrue(reloaded.getNote().isEmpty());
    }

    @Test
    public void execute_noteOnViewedPerson_detailPanelPersonFollowsTheNote() throws Exception {
        model.addPerson(AMY);
        logic.execute("view 1");

        logic.execute("note 1 nt/follow up");

        assertEquals("follow up", logic.getViewedPerson().get().getNote().value);

        logic.execute("note 1 clear");

        assertTrue(logic.getViewedPerson().get().getNote().isEmpty());
    }

    @Test
    public void execute_typedFind_indexAndMutationUseDisplayedContact() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("note 1 nt/React teammate");

        logic.execute("find nt/teammate");
        logic.execute("tag 1 t/featured");

        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals(AMY.getName(), logic.getFilteredPersonList().getFirst().getName());
        assertTrue(logic.getFilteredPersonList().getFirst().getTags().contains(new Tag("featured")));
        logic.execute("note 1 clear");

        assertTrue(logic.getFilteredPersonList().isEmpty());
        assertFalse(model.getAddressBook().getPersonList().get(1).getTags().contains(new Tag("featured")));
    }

    @Test
    public void execute_invalidListCommands_preserveStateAndSavedData() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("note 1 nt/first");
        logic.execute("note 1 nt/second");
        logic.execute("undo");
        logic.execute("find n/Amy");
        logic.execute("sort added");
        logic.execute("view 1");
        AddressBook contactsBefore = new AddressBook(model.getAddressBook());
        List<Person> visibleBefore = List.copyOf(model.getFilteredPersonList());
        Person viewedBefore = model.getViewedPerson().get();
        String statusBefore = model.getPersonListStatus().get();
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedBefore = Files.readString(dataFile);
        String findFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        String spacedPrefixFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommandParser.MESSAGE_SPACED_PREFIX);
        Map<String, String> failures = Map.ofEntries(
                Map.entry("find n /Alex", spacedPrefixFormat),
                Map.entry("find e /x", spacedPrefixFormat),
                Map.entry("find md /2026", spacedPrefixFormat),
                Map.entry("find t /", spacedPrefixFormat),
                Map.entry("find e/p0@example.com", findFormat),
                Map.entry("find n/aisha e/p0@example.com", findFormat),
                Map.entry("find n/aisha p/90000000", findFormat),
                Map.entry("find n/aisha l/user-0x", findFormat),
                Map.entry("find n/", findFormat),
                Map.entry("find private n/secret", FindCommand.MESSAGE_MIXED_SEARCH),
                Map.entry("sort private", String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE)),
                Map.entry("group", String.format(MESSAGE_INVALID_COMMAND_FORMAT, GroupCommand.MESSAGE_USAGE)),
                Map.entry("group t/private!", Tag.MESSAGE_CONSTRAINTS),
                Map.entry("group t/react t/java", Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TAG)),
                Map.entry("view", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE)),
                Map.entry("view 0", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX),
                Map.entry("view 2147483648", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX));

        for (Map.Entry<String, String> failure : failures.entrySet()) {
            assertThrows(ParseException.class, failure.getValue(), () -> logic.execute(failure.getKey()));
            assertEquals(contactsBefore, model.getAddressBook());
            assertEquals(visibleBefore, model.getFilteredPersonList());
            assertEquals(viewedBefore, model.getViewedPerson().get());
            assertEquals(statusBefore, model.getPersonListStatus().get());
            assertEquals(savedBefore, Files.readString(dataFile));
            assertTrue(model.canUndo());
            assertTrue(model.canRedo());
        }

        assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> logic.execute("view 2"));
        assertEquals(contactsBefore, model.getAddressBook());
        assertEquals(viewedBefore, model.getViewedPerson().get());
        assertEquals(visibleBefore, model.getFilteredPersonList());
        assertEquals(savedBefore, Files.readString(dataFile));
        assertEquals(statusBefore, model.getPersonListStatus().get());
        logic.execute("redo");
        assertEquals("second", model.getAddressBook().getPersonList().getFirst().getNote().value);
        logic.execute("undo");
        assertEquals("first", model.getAddressBook().getPersonList().getFirst().getNote().value);
    }

    @Test
    public void execute_viewEmptyResultsThenList_preservesSelectionAndRestoresDisplayedIndexes() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("view 2");
        logic.execute("find n/zzzz");
        Person viewedBefore = model.getViewedPerson().get();
        AddressBook contactsBefore = new AddressBook(model.getAddressBook());
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedBefore = Files.readString(dataFile);
        String statusBefore = model.getPersonListStatus().get();

        assertThrows(CommandException.class, MESSAGE_EMPTY_PERSON_LIST, () -> logic.execute("view 1"));

        assertEquals(contactsBefore, model.getAddressBook());
        assertEquals(viewedBefore, model.getViewedPerson().get());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(statusBefore, model.getPersonListStatus().get());
        assertEquals(savedBefore, Files.readString(dataFile));
        assertFalse(model.canUndo());
        assertFalse(model.canRedo());

        assertEquals("Listed all contacts.", logic.execute("list extra").getFeedbackToUser());
        assertEquals(contactsBefore.getPersonList(), model.getFilteredPersonList());
        assertEquals(viewedBefore, model.getViewedPerson().get());
        logic.execute("view 1");
        assertEquals(AMY.getName(), model.getViewedPerson().get().getName());
    }

    @Test
    public void execute_editAndTagKeepTheNote() throws Exception {
        model.addPerson(AMY);
        logic.execute("note 1 nt/keep me");

        logic.execute("edit 1 p/91234567");
        logic.execute("tag 1 t/friends");

        assertEquals("keep me", logic.getFilteredPersonList().get(0).getNote().value);
    }

    @Test
    public void execute_noteCommandErrors_leaveTheNoteUnchanged() throws Exception {
        model.addPerson(AMY);
        logic.execute("note 1 nt/original");

        assertParseException("note 1 nt/   ", Note.MESSAGE_BLANK);
        assertParseException("note 1 nt/" + "a".repeat(Note.MAX_LENGTH + 1), Note.MESSAGE_TOO_LONG);
        assertCommandException("note 9 nt/new", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        assertEquals("original", logic.getFilteredPersonList().get(0).getNote().value);
    }

    @Test
    public void execute_noteCommands_neverWriteTheNoteTextToTheLog() throws Exception {
        model.addPerson(AMY);
        String secret = "private text";
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
        // the loggers of the classes that log a command, at the most detailed level
        Logger[] loggers = {Logger.getLogger("ab3.LogicManager"), Logger.getLogger("ab3.AddressBookParser")};
        Level[] oldLevels = {loggers[0].getLevel(), loggers[1].getLevel()};
        for (Logger logger : loggers) {
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);
        }

        try {
            logic.execute("note 1 nt/" + secret);
            assertParseException("note 1 " + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX));
            assertParseException("note 1 nt/" + (secret + " ").repeat(100), Note.MESSAGE_TOO_LONG);
            assertCommandException("note 9 nt/" + secret, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            logic.execute("NOTE 1 nt/" + secret);
            assertParseException("NOTE 1 NT/" + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX));
            logic.execute("find nt/" + secret);
            assertParseException("find NT/" + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            assertParseException("FIND Nt/" + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        } finally {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].removeHandler(handler);
                loggers[i].setLevel(oldLevels[i]);
            }
        }

        assertFalse(logged.isEmpty());
        assertTrue(logged.stream().anyMatch(message -> message.contains("note [hidden]")));
        assertTrue(logged.stream().noneMatch(message -> message.contains(secret)), logged.toString());
    }
    @Test
    public void execute_meetingCommands_neverWriteMeetingTextToTheLog() throws Exception {
        model.addPerson(AMY);
        String secret = "private meeting text";
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
        // the loggers of the classes that log a command, at the most detailed level
        Logger[] loggers = {Logger.getLogger("ab3.LogicManager"), Logger.getLogger("ab3.AddressBookParser")};
        Level[] oldLevels = {loggers[0].getLevel(), loggers[1].getLevel()};
        for (Logger logger : loggers) {
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);
        }

        try {
            logic.execute("meeting 1 ev/" + secret + " r/" + secret);
            assertParseException("meeting 1 " + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            MeetingCommand.MESSAGE_WITHOUT_PREFIX));
            assertParseException("meeting 1 ev/" + secret + " ev/duplicate",
                    Messages.getErrorMessageForDuplicatePrefixes(
                            CliSyntax.PREFIX_EVENT));
            assertCommandException("meeting 9 ev/" + secret, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            logic.execute("find ev/" + secret);
            assertParseException("find Alice ev/" + secret, FindCommand.MESSAGE_MIXED_SEARCH);
            assertParseException("find EV/" + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        } finally {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].removeHandler(handler);
                loggers[i].setLevel(oldLevels[i]);
            }
        }

        assertFalse(logged.isEmpty());
        assertTrue(logged.stream().anyMatch(message -> message.contains("meeting [hidden]")));
        assertTrue(logged.stream().anyMatch(message -> message.contains("find [hidden]")));
        assertTrue(logged.stream().noneMatch(message -> message.contains(secret)), logged.toString());
    }
}
