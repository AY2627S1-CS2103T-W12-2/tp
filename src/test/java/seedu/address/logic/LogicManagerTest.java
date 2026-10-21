package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_EMPTY_PERSON_LIST;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;
import static seedu.address.testutil.TypicalTimes.NOW;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
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
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
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
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
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

        assertThrows(CommandException.class, String.format(LogicManager.FILE_OPS_ERROR_FORMAT,
                DUMMY_IO_EXCEPTION.getMessage()), () -> logic.execute(command));

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
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
            assertParseException("note 1 nt/" + (secret + " ").repeat(100), Note.MESSAGE_TOO_LONG);
            assertCommandException("note 9 nt/" + secret, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
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
                            MeetingCommand.MESSAGE_USAGE));
            assertParseException("meeting 1 ev/" + secret + " ev/duplicate",
                    Messages.getErrorMessageForDuplicatePrefixes(
                            CliSyntax.PREFIX_EVENT));
            assertCommandException("meeting 9 ev/" + secret, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            logic.execute("find ev/" + secret);
            assertParseException("find Alice ev/" + secret,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            FindCommand.MESSAGE_USAGE));
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
