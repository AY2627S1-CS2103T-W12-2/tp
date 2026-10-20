package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.StorageManager;

public class MeetingPhotoIntegrationTest {
    @TempDir
    private Path folder;

    @Test
    public void execute_meetingPhotoEditTagAndHistory_preserveBothFeaturesAndReload() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        ModelManager model = new ModelManager(book, new UserPrefs(), FIXED_CLOCK);
        JsonAddressBookStorage saved = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(saved,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))), new PhotoStorage(folder));
        ImageIO.write(new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("portrait.png").toFile());
        logic.execute("note 1 nt/Existing team note");
        logic.execute("meeting 1 ev/Hack Singapore md/2026-10-20 r/Mentor");
        var meeting = model.getFilteredPersonList().getFirst().getMeeting();
        logic.execute("photo 1 f/portrait.png");
        String reference = model.getFilteredPersonList().getFirst().getPhotoPath();
        logic.execute("edit 1 e/updated@example.com");
        logic.execute("tag 1 t/mentor");
        var updated = model.getFilteredPersonList().getFirst();
        assertEquals("Existing team note", updated.getNote().value);
        assertEquals(meeting, updated.getMeeting());
        assertEquals(reference, updated.getPhotoPath());
        assertEquals(updated, saved.readAddressBook().orElseThrow().getPersonList().getFirst());
        logic.execute("meeting 1 clear");
        assertEquals(reference, model.getFilteredPersonList().getFirst().getPhotoPath());
        logic.execute("undo");
        assertEquals(meeting, model.getFilteredPersonList().getFirst().getMeeting());
        logic.execute("photo 1 clear");
        assertEquals(meeting, model.getFilteredPersonList().getFirst().getMeeting());
        logic.execute("undo");
        assertEquals(reference, model.getFilteredPersonList().getFirst().getPhotoPath());
        model.redo();
        assertTrue(model.getFilteredPersonList().getFirst().getPhotoPath().isEmpty());
        assertEquals(meeting, model.getFilteredPersonList().getFirst().getMeeting());
        assertTrue(Files.exists(folder.resolve(reference)));
        assertFalse(meeting.isEmpty());
    }
}
