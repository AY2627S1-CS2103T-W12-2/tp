package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import seedu.address.storage.PhotoStorage;
import seedu.address.testutil.PersonBuilder;

public class ContactPhotoTest {
    @TempDir
    private Path folder;

    @BeforeAll
    public static void startJavaFxToolkit() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test already started the toolkit.
        }
    }

    @Test
    public void show_cardUsesBoundedThumbnailAndPlaceholder() throws Exception {
        var card = new PersonCard(new PersonBuilder().build(), 1);
        ImageView image = (ImageView) card.getRoot().lookup("#photo");
        Label placeholder = (Label) card.getRoot().lookup("#photoPlaceholder");
        assertEquals(48, image.getFitWidth());
        assertEquals(48, image.getFitHeight());
        assertTrue(image.isPreserveRatio());
        assertTrue(placeholder.isVisible());
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("face.png").toFile());
        ContactPhoto.show("face.png", image, placeholder, new PhotoStorage(folder));
        assertNotNull(image.getImage());
        assertTrue(image.isVisible());
        ContactPhoto.show("../unsafe.png", image, placeholder, new PhotoStorage(folder));
        assertTrue(placeholder.isVisible());
    }

    @Test
    public void constructor_detailUsesAspectPreservingBoundedImageInScrollPane() {
        var panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));
        var scroll = (ScrollPane) panel.getRoot().lookup(".scroll-pane");
        ImageView image = (ImageView) scroll.getContent().lookup("#photo");
        assertEquals(240, image.getFitWidth());
        assertEquals(180, image.getFitHeight());
        assertTrue(image.isPreserveRatio());
        assertTrue(scroll.isFitToWidth());
    }
}
