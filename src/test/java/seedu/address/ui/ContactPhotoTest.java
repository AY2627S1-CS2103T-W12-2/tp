package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
        var card = onFx(() -> new PersonCard(new PersonBuilder().build(), 1));
        ImageView image = (ImageView) card.getRoot().lookup("#photo");
        Label placeholder = (Label) card.getRoot().lookup("#photoPlaceholder");
        onFx(() -> {
            assertEquals(48, image.getFitWidth());
            assertEquals(48, image.getFitHeight());
            assertTrue(image.isPreserveRatio());
            assertTrue(placeholder.isVisible());
            return null;
        });
        ImageIO.write(new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("face.png").toFile());
        onFx(() -> ContactPhoto.show("face.png", image, placeholder, new PhotoStorage(folder)))
                .get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertNotNull(image.getImage());
            assertTrue(image.isVisible());
            assertTrue(image.getImage().getWidth() <= 48);
            assertTrue(image.getImage().getHeight() <= 48);
            return null;
        });
        onFx(() -> ContactPhoto.show("../unsafe.png", image, placeholder, new PhotoStorage(folder)))
                .get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertTrue(placeholder.isVisible());
            assertNull(image.getImage());
            return null;
        });
    }

    @Test
    public void show_slowPhoto_keepsUiResponsiveAndDiscardsStaleResult() throws Exception {
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("face.png").toFile());
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean readOnFxThread = new AtomicBoolean(true);
        PhotoStorage slowStorage = new PhotoStorage(folder) {
            @Override
            public Optional<byte[]> readPhoto(String reference) {
                readOnFxThread.set(Platform.isFxApplicationThread());
                reading.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new AssertionError("Photo read was not released");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
                return super.readPhoto(reference);
            }
        };
        ImageView image = onFx(() -> {
            ImageView view = new ImageView();
            view.setFitWidth(48);
            view.setFitHeight(48);
            return view;
        });
        Label placeholder = onFx(Label::new);
        CompletableFuture<Void> pending = onFx(() -> ContactPhoto.show("face.png", image, placeholder, slowStorage));
        try {
            assertTrue(reading.await(10, TimeUnit.SECONDS));
            assertFalse(readOnFxThread.get());
            // This FX task completes while file reading is blocked, and clears the previous request.
            onFx(() -> ContactPhoto.show("", image, placeholder, slowStorage)).get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
        }
        pending.get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertNull(image.getImage());
            assertTrue(placeholder.isVisible());
            assertFalse(image.isVisible());
            return null;
        });
    }

    @Test
    public void show_rapidRefreshes_discardsObsoleteQueuedReads() throws Exception {
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB),
                "png", folder.resolve("face.png").toFile());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<String> reads = new ArrayList<>();
        PhotoStorage storage = new PhotoStorage(folder) {
            @Override
            public Optional<byte[]> readPhoto(String reference) {
                reads.add(reference);
                if (reference.equals("blocking.png")) {
                    started.countDown();
                    try {
                        if (!release.await(10, TimeUnit.SECONDS)) {
                            throw new AssertionError("Photo read was not released");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }
                return super.readPhoto("face.png");
            }
        };
        onFx(() -> ContactPhoto.show("blocking.png", new ImageView(), new Label(), storage));
        CompletableFuture<Void> latest;
        try {
            assertTrue(started.await(10, TimeUnit.SECONDS));
            latest = onFx(() -> {
                ImageView image = new ImageView();
                Label placeholder = new Label();
                for (int i = 0; i < 20; i++) {
                    ContactPhoto.show("obsolete-" + i + ".png", image, placeholder, storage);
                }
                ImageView discardedCard = new ImageView();
                CompletableFuture<Void> discarded = ContactPhoto.show("discarded-card.png", discardedCard,
                        new Label(), storage);
                ContactPhoto.cancel(discardedCard);
                assertTrue(discarded.isDone());
                return ContactPhoto.show("latest.png", image, placeholder, storage);
            });
        } finally {
            release.countDown();
        }
        latest.get(10, TimeUnit.SECONDS);
        assertEquals(List.of("blocking.png", "latest.png"), reads);
    }

    @Test
    public void show_failedBackgroundRead_keepsPlaceholder() throws Exception {
        PhotoStorage failingStorage = new PhotoStorage(folder) {
            @Override
            public Optional<byte[]> readPhoto(String reference) {
                throw new IllegalStateException("Simulated read failure");
            }
        };
        ImageView image = onFx(ImageView::new);
        Label placeholder = onFx(Label::new);
        onFx(() -> ContactPhoto.show("face.png", image, placeholder, failingStorage)).get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertNull(image.getImage());
            assertTrue(placeholder.isVisible());
            return null;
        });
    }

    @Test
    public void constructor_detailUsesAspectPreservingBoundedImageInScrollPane() throws Exception {
        onFx(() -> {
            var panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));
            var scroll = (ScrollPane) panel.getRoot().lookup(".scroll-pane");
            ImageView image = (ImageView) scroll.getContent().lookup("#photo");
            assertEquals(240, image.getFitWidth());
            assertEquals(180, image.getFitHeight());
            assertTrue(image.isPreserveRatio());
            assertTrue(scroll.isFitToWidth());
            return null;
        });
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
