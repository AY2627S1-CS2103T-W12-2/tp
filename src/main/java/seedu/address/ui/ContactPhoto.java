package seedu.address.ui;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seedu.address.commons.core.LogsCenter;
import seedu.address.storage.PhotoStorage;

/** Bounded photo rendering for the detail panel. File access is confined to validated storage. */
final class ContactPhoto {
    private static final Logger logger = LogsCenter.getLogger(ContactPhoto.class);
    private static final Object REQUEST_KEY = new Object();
    // One decoder bounds peak memory even when a list contains many large photos.
    private static final ThreadPoolExecutor loader = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), task -> {
                Thread thread = new Thread(task, "contact-photo-loader");
                thread.setDaemon(true);
                return thread;
            });

    private ContactPhoto() {
    }

    /**
     * Shows a placeholder immediately, then validates and decodes off the JavaFX thread.
     * Replaced requests are removed from the queue. The future completes when the result is
     * applied or the request is cancelled; an already-running decode may finish without displaying.
     */
    static CompletableFuture<Void> show(String reference, ImageView imageView, Label placeholder,
            PhotoStorage storage) {
        cancel(imageView);
        update(null, imageView, placeholder);
        if (reference.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        double width = imageView.getFitWidth();
        double height = imageView.getFitHeight();
        PhotoRequest request = new PhotoRequest();
        imageView.getProperties().put(REQUEST_KEY, request);
        request.loading = new FutureTask<>(() -> {
            Image image = null;
            try {
                image = storage.readPhoto(reference)
                        .map(bytes -> new Image(new ByteArrayInputStream(bytes), width, height, true, true))
                        .filter(loaded -> !loaded.isError()).orElse(null);
            } catch (RuntimeException e) {
                logger.warning("Contact photo could not be loaded.");
            }
            Image result = image;
            Platform.runLater(() -> {
                if (imageView.getProperties().get(REQUEST_KEY) == request) {
                    imageView.getProperties().remove(REQUEST_KEY);
                    update(result, imageView, placeholder);
                }
                request.displayed.complete(null);
            });
            return null;
        });
        loader.execute(request.loading);
        return request.displayed;
    }

    /** Discards queued work for a replaced image or card, without interrupting an active image decoder. */
    static void cancel(ImageView imageView) {
        Object pending = imageView.getProperties().remove(REQUEST_KEY);
        if (pending instanceof PhotoRequest request) {
            request.loading.cancel(false);
            loader.remove(request.loading);
            request.displayed.complete(null);
        }
    }

    /** Applies only the already-decoded image on the JavaFX thread. */
    private static void update(Image image, ImageView imageView, Label placeholder) {
        imageView.setImage(image);
        imageView.setVisible(image != null);
        imageView.setManaged(image != null);
        placeholder.setVisible(image == null);
        placeholder.setManaged(image == null);
    }

    private static class PhotoRequest {
        private final CompletableFuture<Void> displayed = new CompletableFuture<>();
        private FutureTask<Void> loading;
    }
}
