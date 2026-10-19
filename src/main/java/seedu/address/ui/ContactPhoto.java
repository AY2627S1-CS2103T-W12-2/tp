package seedu.address.ui;

import java.io.ByteArrayInputStream;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seedu.address.storage.PhotoStorage;

/** Shared bounded photo rendering for cards and details. File access is confined to validated storage. */
final class ContactPhoto {
    private ContactPhoto() {
    }

    /** Updates both the image and its visible fallback without passing untrusted paths to JavaFX. */
    static void show(String reference, ImageView imageView, Label placeholder, PhotoStorage storage) {
        Image image = storage.readPhoto(reference)
                .map(bytes -> new Image(new ByteArrayInputStream(bytes),
                        imageView.getFitWidth(), imageView.getFitHeight(), true, true))
                .filter(loaded -> !loaded.isError()).orElse(null);
        imageView.setImage(image);
        imageView.setVisible(image != null);
        imageView.setManaged(image != null);
        placeholder.setVisible(image == null);
        placeholder.setManaged(image == null);
    }
}
