package seedu.address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    /** The release version reported when LarperBoard starts. */
    public static final String VERSION = "v1.4";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private String startupWarning = "";

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing LarperBoard ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath(), startupWarning);
    }

    /**
     * Creates a model from saved data, sample data if no file exists, or an empty book if loading fails.
     *
     * @param storage source of the saved address book
     * @param userPrefs preferences to use in the model
     * @return the initialized model
     */
    Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());
        startupWarning = "";

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with sample LarperBoard data.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            Path backup = backupInvalidData(storage.getAddressBookFilePath());
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Preserved at " + backup + ". Will be starting with an empty LarperBoard.");
            String reason = e.getCause() instanceof IllegalValueException
                    ? e.getCause().getMessage() : "The data file could not be read or parsed.";
            startupWarning = reason + "\nLarperBoard opened with an empty contact list. "
                    + "The original file is preserved at " + backup + ". "
                    + "The next successful command, including help or list, replaces the data file. "
                    + "Close LarperBoard and correct a copy of the backup before using commands.";
            initialData = new AddressBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    String getStartupWarning() {
        return startupWarning;
    }

    private Path backupInvalidData(Path dataFile) throws IOException {
        Path source = dataFile.toAbsolutePath().normalize();
        String backupName = source.getFileName() + ".bak";
        for (int suffix = 0; ; suffix++) {
            Path backup = source.resolveSibling(backupName + (suffix == 0 ? "" : "." + suffix));
            if (Files.exists(backup)) {
                if (Files.isSymbolicLink(backup) || Files.isSameFile(source, backup)) {
                    continue;
                }
                if (Files.mismatch(source, backup) == -1) {
                    return backup;
                }
            } else {
                return Files.copy(source, backup, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting LarperBoard " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping LarperBoard ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
