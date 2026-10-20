package seedu.address.ui;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seedu.address.commons.core.LogsCenter;
import seedu.address.storage.PhotoStorage;

/** Shared bounded photo rendering for cards and details. File access is confined to validated storage. */
final class ContactPhoto {
    private static final Logger logger = LogsCenter.getLogger(ContactPhoto.class);
    private static final Object REQUEST_KEY = new Object();
    // One decoder bounds peak memory even when a list contains many large photos.
    private static final ExecutorService loader = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "contact-photo-loader");
        thread.setDaemon(true);
        return thread;
    });

    private ContactPhoto() {
    }

    /**
     * Shows a placeholder immediately, then validates and decodes off the JavaFX thread.
     * Only the latest request for a view may publish its result. The returned future completes
     * after the result is applied (or discarded), and allows callers to observe loading completion.
     */
    static CompletableFuture<Void> show(String reference, ImageView imageView, Label placeholder,
            PhotoStorage storage) {
        Object request = new Object();
        imageView.getProperties().put(REQUEST_KEY, request);
        update(null, imageView, placeholder);
        if (reference.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        double width = imageView.getFitWidth();
        double height = imageView.getFitHeight();
        CompletableFuture<Void> displayed = new CompletableFuture<>();
        CompletableFuture.supplyAsync(() -> storage.readPhoto(reference)
                .map(bytes -> new Image(new ByteArrayInputStream(bytes), width, height, true, true))
                .filter(image -> !image.isError()).orElse(null), loader)
                .whenComplete((image, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        logger.warning("Contact photo could not be loaded.");
                    }
                    if (imageView.getProperties().get(REQUEST_KEY) == request) {
                        update(image, imageView, placeholder);
                    }
                    displayed.complete(null);
                }));
        return displayed;
    }

    /** Applies only the already-decoded image on the JavaFX thread. */
    private static void update(Image image, ImageView imageView, Label placeholder) {
        imageView.setImage(image);
        imageView.setVisible(image != null);
        imageView.setManaged(image != null);
        placeholder.setVisible(image == null);
        placeholder.setManaged(image == null);
    }
}
