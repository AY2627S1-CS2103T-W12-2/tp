package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies that an accent typed as a separate character is the same name as the composed accent, whether it
 * comes from a command, a search or the data file.
 */
public class NameNormalizationTest {
    private static final String COMPOSED = "Jos\u00e9 Tan";
    private static final String DECOMPOSED = "Jose\u0301 Tan";

    @TempDir
    private Path folder;

    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        logic = newLogic(model);
    }

    private LogicManager newLogic(ModelManager modelToUse) {
        return new LogicManager(modelToUse, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("contacts.json")),
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void execute_addWithSeparateAccent_isStoredComposedAndFoundByEitherSpelling() throws Exception {
        logic.execute("add n/" + DECOMPOSED + " p/91234567");
        assertEquals(List.of(COMPOSED), names(model));

        for (String search : new String[] {"find " + DECOMPOSED.split(" ")[0], "find " + COMPOSED.split(" ")[0],
            "find n/" + DECOMPOSED.split(" ")[0], "find n/" + COMPOSED.split(" ")[0]}) {
            logic.execute("list");
            logic.execute(search);
            assertEquals(1, model.getFilteredPersonList().size(), search);
        }
    }

    @Test
    public void execute_searchWithoutTheAccent_stillDiffersFromTheAccentedName() throws Exception {
        logic.execute("add n/" + DECOMPOSED + " p/91234567");

        logic.execute("find Jose");
        assertEquals(0, model.getFilteredPersonList().size());
        logic.execute("find n/Jose Tan");
        assertEquals(0, model.getFilteredPersonList().size());
    }

    @Test
    public void readAddressBook_fileWithSeparateAccent_loadsComposedAndIsStableWhenSavedAgain() throws Exception {
        ModelManager loaded = loadFileWithSeparateAccent();

        assertEquals(List.of(COMPOSED), names(loaded));
        Path again = folder.resolve("again.json");
        new JsonAddressBookStorage(again).saveAddressBook(loaded.getAddressBook());
        String json = Files.readString(again, StandardCharsets.UTF_8);
        assertTrue(json.contains(COMPOSED));
        assertFalse(json.contains(DECOMPOSED));
    }

    @Test
    public void execute_addDuplicateOfContactLoadedWithSeparateAccent_isRejected() throws Exception {
        ModelManager loaded = loadFileWithSeparateAccent();
        LogicManager loadedLogic = newLogic(loaded);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                loadedLogic.execute("add n/" + COMPOSED + " p/91234567"));
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                loadedLogic.execute("add n/" + DECOMPOSED + " p/91234567"));
        assertEquals(1, loaded.getAddressBook().getPersonList().size());
    }

    /** Saves one contact, rewrites its accent as a separate character in the file, and loads the file. */
    private ModelManager loadFileWithSeparateAccent() throws Exception {
        Path file = folder.resolve("separate-accent.json");
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName(COMPOSED).withPhone("91234567").build());
        new JsonAddressBookStorage(file).saveAddressBook(original);
        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json.contains(COMPOSED));
        Files.writeString(file, json.replace(COMPOSED, DECOMPOSED), StandardCharsets.UTF_8);

        AddressBook readBack = new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        return new ModelManager(readBack, new UserPrefs(), FIXED_CLOCK);
    }

    private static List<String> names(ModelManager modelToRead) {
        return modelToRead.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList();
    }
}
