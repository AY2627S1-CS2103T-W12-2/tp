package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

/**
 * Tests LarperBoard's startup data selection and UI lifecycle.
 */
public class MainAppTest {

    @TempDir
    public Path testFolder;

    private final MainApp app = new MainApp();

    @Test
    public void version_matchesRelease() {
        assertEquals("v1.4", MainApp.VERSION);
    }

    @Test
    public void mainWindowFxml_hasLarperBoardTitle() throws IOException {
        try (InputStream fxml = MainApp.class.getResourceAsStream("/view/MainWindow.fxml")) {
            assertNotNull(fxml);
            assertTrue(new String(fxml.readAllBytes(), StandardCharsets.UTF_8).contains("title=\"LarperBoard\""));
        }
    }

    @Test
    public void init_initializesAppComponents() throws Exception {
        MainApp appWithIsolatedData = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                return new UserPrefs();
            }

            @Override
            Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
                return new ModelManager();
            }
        };

        appWithIsolatedData.init();

        assertNotNull(appWithIsolatedData.storage);
        assertNotNull(appWithIsolatedData.model);
        assertNotNull(appWithIsolatedData.logic);
        assertNotNull(appWithIsolatedData.ui);
    }

    @Test
    public void initModelManager_missingData_usesSampleData() throws IOException {
        Model model = app.initModelManager(createStorage(), new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
    }

    @Test
    public void initModelManager_savedData_usesSavedData() throws IOException {
        Storage storage = createStorage();
        storage.saveAddressBook(getTypicalAddressBook());

        Model model = app.initModelManager(storage, new UserPrefs());

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void initModelManager_invalidData_usesEmptyAddressBook() throws IOException {
        Files.writeString(testFolder.resolve("addressbook.json"), "{invalid json");

        Model model = app.initModelManager(createStorage(), new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void initModelManager_invalidData_preservesOriginalBeforeNextCommand() throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        String invalidData = "{invalid json";
        Files.writeString(dataFile, invalidData);
        Storage storage = createStorage();

        Model model = app.initModelManager(storage, new UserPrefs());
        new LogicManager(model, storage).execute("list");

        assertEquals(invalidData, Files.readString(testFolder.resolve("addressbook.json.bak")));
        assertFalse(Files.readString(dataFile).equals(invalidData));
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void initModelManager_invalidField_preservesAllOriginalContacts() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        String invalidData = "{\"persons\":["
                + "{\"name\":\"Alice Pauline\",\"phone\":\"94351253\",\"email\":\"alice@example.com\"},"
                + "{\"name\":\"Bob Choo\",\"phone\":\"93210283\",\"email\":\"bob@example.com\","
                + "\"followUpDate\":\"2026-02-30\"}]}";
        Files.writeString(dataFile, invalidData);

        Model model = app.initModelManager(createStorage(), new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidData, Files.readString(testFolder.resolve("addressbook.json.bak")));
        assertEquals(invalidData, Files.readString(dataFile));
        assertTrue(app.getStartupWarning().contains("followUpDate"));
        assertTrue(app.getStartupWarning().contains("addressbook.json.bak"));
    }

    @Test
    public void initModelManager_existingBackup_keepsItAndCreatesNumberedBackup() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{new invalid json");
        Path earlierBackup = testFolder.resolve("addressbook.json.bak");
        Files.writeString(earlierBackup, "{older invalid json");

        app.initModelManager(createStorage(), new UserPrefs());

        assertEquals("{older invalid json", Files.readString(earlierBackup));
        assertEquals("{new invalid json", Files.readString(testFolder.resolve("addressbook.json.bak.1")));
    }

    @Test
    public void initModelManager_repeatedInvalidStartup_reusesIdenticalBackup() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{invalid json");

        app.initModelManager(createStorage(), new UserPrefs());
        app.initModelManager(createStorage(), new UserPrefs());

        assertTrue(Files.exists(testFolder.resolve("addressbook.json.bak")));
        assertFalse(Files.exists(testFolder.resolve("addressbook.json.bak.1")));
    }

    @Test
    public void initModelManager_validData_hasNoStartupWarning() throws IOException {
        Storage storage = createStorage();
        storage.saveAddressBook(getTypicalAddressBook());

        app.initModelManager(storage, new UserPrefs());

        assertTrue(app.getStartupWarning().isEmpty());
        assertFalse(Files.exists(testFolder.resolve("addressbook.json.bak")));
    }

    @Test
    public void initModelManager_backupFails_doesNotStartWithEmptyModel() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{invalid json");
        Files.createDirectory(testFolder.resolve("addressbook.json.bak"));

        assertThrows(IOException.class, () -> app.initModelManager(createStorage(), new UserPrefs()));
        assertEquals("{invalid json", Files.readString(dataFile));
    }

    @Test
    public void start_startsUi() {
        AtomicBoolean uiStarted = new AtomicBoolean();
        app.ui = primaryStage -> uiStarted.set(true);

        app.start(null);

        assertTrue(uiStarted.get());
    }

    @Test
    public void stop_savesUserPreferences() throws DataLoadingException {
        app.model = new ModelManager();
        app.storage = createStorage();

        app.stop();

        assertEquals(app.model.getUserPrefs(), app.storage.readUserPrefs().orElseThrow());
    }

    private Storage createStorage() {
        return new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
