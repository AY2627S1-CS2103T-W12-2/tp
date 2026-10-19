package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.StorageManager;

public class PhotoFeatureTest {
    @TempDir
    private Path folder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private boolean failSave;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        model = new ModelManager(book, new UserPrefs(), FIXED_CLOCK);
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave) {
                    throw new IOException("test failure");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))), new PhotoStorage(folder));
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("source.png").toFile());
    }

    @Test
    public void execute_attachClearUndo_retainsCopiedImageAndReloads() throws Exception {
        logic.execute("photo 1 f/source.png");
        String path = model.getFilteredPersonList().getFirst().getPhotoPath();
        assertEquals(path, storage.readAddressBook().orElseThrow().getPersonList().getFirst().getPhotoPath());
        logic.execute("photo 1 clear");
        assertTrue(model.getFilteredPersonList().getFirst().getPhotoPath().isEmpty());
        assertTrue(Files.exists(folder.resolve(path)));
        logic.execute("undo");
        assertEquals(path, model.getFilteredPersonList().getFirst().getPhotoPath());
        logic.execute("delete 1");
        logic.execute("undo");
        assertEquals(path, model.getFilteredPersonList().getFirst().getPhotoPath());
    }

    @Test
    public void execute_duplicateAndEmptyClear_doNotEnterHistory() throws Exception {
        logic.execute("photo 1 clear");
        assertFalse(model.canUndo());
        logic.execute("photo 1 f/source.png");
        var attached = model.getFilteredPersonList().getFirst();
        logic.execute("photo 1 f/source.png");
        assertEquals(attached, model.getFilteredPersonList().getFirst());
        logic.execute("undo");
        assertFalse(model.canUndo());
        assertTrue(model.getFilteredPersonList().getFirst().getPhotoPath().isEmpty());
    }

    @Test
    public void execute_filteredIndex_updatesOnlyDisplayedContact() throws Exception {
        logic.execute("find Benson");
        logic.execute("photo 1 f/source.png");
        assertEquals(BENSON.getName(), model.getFilteredPersonList().getFirst().getName());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getPhotoPath().isEmpty());
    }

    @Test
    public void execute_invalidInputAndFailedSave_preserveModelAndHistory() {
        for (String args : new String[]{"", "1", "0 f/source.png", "1 f/", "1 clear extra",
            "1 f/source.png f/source.png", "1 clear f/source.png"}) {
            assertThrows(ParseException.class, () -> logic.execute("photo " + args));
        }
        assertThrows(CommandException.class, () -> logic.execute("photo 3 f/source.png"));
        failSave = true;
        assertThrows(CommandException.class, () -> logic.execute("photo 1 f/source.png"));
        assertEquals(ALICE, model.getFilteredPersonList().getFirst());
        assertFalse(model.canUndo());
    }
}
