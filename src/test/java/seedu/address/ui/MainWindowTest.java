package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    /**
     * Starts the JavaFX toolkit, which is needed to create UI controls.
     * The tests are skipped in environments without a display (e.g. Linux CI runners).
     */
    @BeforeAll
    public static void startJavaFxToolkit() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit already started
        }
        // closing the only window must not shut the toolkit down for the tests that run after it
        Platform.setImplicitExit(false);
    }

    @Test
    public void fillInnerParts_detailPanelFollowsViewedPerson() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(new ModelManager(getTypicalAddressBook(), new UserPrefs()), storage);

        runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), logic, dataFile);
            mainWindow.fillInnerParts();
            StackPane detailPanel = (StackPane) mainWindow.getPrimaryStage().getScene().getRoot()
                    .lookup("#personDetailPanelPlaceholder");

            assertEquals(1, detailPanel.getChildren().size());
            assertTrue(detailPanel.lookup("#placeholder").isVisible());

            logic.execute("view 1");

            // the scroll pane only adds its content to the scene graph once it has been laid out
            detailPanel.applyCss();
            detailPanel.layout();

            assertFalse(detailPanel.lookup("#placeholder").isVisible());
            detailPanel.applyCss();
            String viewedName = logic.getFilteredPersonList().get(0).getName().fullName;
            assertEquals(viewedName, ((Label) detailPanel.lookup("#name")).getText());
        });
    }

    @Test
    public void executeCommand_failedNoteCommand_logsTheCommandWithoutTheNoteText() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic logic = new LogicManager(new ModelManager(getTypicalAddressBook(), new UserPrefs()), storage);
        List<String> logged = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logged.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };

        runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), logic, dataFile);
            mainWindow.fillInnerParts();
            // the window gets its logger when it is created, so the handler is added afterwards
            Logger logger = Logger.getLogger("ab3.MainWindow");
            Level oldLevel = logger.getLevel();
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);
            try {
                TextField commandBox = (TextField) mainWindow.getPrimaryStage().getScene().getRoot()
                        .lookup("#commandTextField");
                // a note command with a bad index only fails once it is executed
                commandBox.setText("note 99 nt/private text");
                commandBox.fireEvent(new ActionEvent());
            } finally {
                logger.removeHandler(handler);
                logger.setLevel(oldLevel);
            }
        });

        assertTrue(logged.stream().anyMatch(message -> message.contains("note [hidden]")), logged.toString());
        assertTrue(logged.stream().noneMatch(message -> message.contains("private text")), logged.toString());
    }

    @Test
    public void executeCommand_exit_keepsWindowOpenLongEnoughToReadTheMessageThenCloses() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        Logic logic = newLogic(dataFile);
        CountDownLatch closed = new CountDownLatch(1);

        runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), logic, dataFile);
            mainWindow.fillInnerParts();
            mainWindow.show();
            Stage stage = mainWindow.getPrimaryStage();
            stage.showingProperty().addListener((observable, wasShowing, isShowing) -> {
                if (!isShowing) {
                    closed.countDown();
                }
            });
            TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
            commandBox.setText("exit");
            commandBox.fireEvent(new ActionEvent());

            // straight after the command the message is on screen, the window is still open and takes no input
            TextArea resultDisplay = (TextArea) stage.getScene().getRoot().lookup("#resultDisplay");
            assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, resultDisplay.getText());
            assertTrue(stage.isShowing());
            assertTrue(commandBox.isDisabled());
        });

        assertTrue(closed.await(5, TimeUnit.SECONDS), "The window should close after the pause");
    }

    @Test
    public void closeRequest_duringTheExitPause_closesAtOnce() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        Logic logic = newLogic(dataFile);

        runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), logic, dataFile);
            mainWindow.fillInnerParts();
            mainWindow.show();
            Stage stage = mainWindow.getPrimaryStage();
            TextField commandBox = (TextField) stage.getScene().getRoot().lookup("#commandTextField");
            commandBox.setText("exit");
            commandBox.fireEvent(new ActionEvent());
            assertTrue(stage.isShowing());

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

            assertFalse(stage.isShowing());
        });
    }

    @Test
    public void closeRequest_withoutExitCommand_closesAtOnce() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        Logic logic = newLogic(dataFile);

        runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), logic, dataFile);
            mainWindow.fillInnerParts();
            mainWindow.show();
            Stage stage = mainWindow.getPrimaryStage();
            assertTrue(stage.isShowing());

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

            assertFalse(stage.isShowing());
        });
    }

    private Logic newLogic(Path dataFile) {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        return new LogicManager(new ModelManager(getTypicalAddressBook(), new UserPrefs()), storage);
    }

    /**
     * Runs {@code action} on the JavaFX application thread, and rethrows anything it throws.
     */
    private static void runOnFxThread(ThrowingRunnable action) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(10, TimeUnit.SECONDS), "Timed out waiting for the JavaFX thread");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
