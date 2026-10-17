package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

    private final List<String> executedCommands = new ArrayList<>();

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
    }

    @Test
    public void upKey_noHistory_leavesTextUnchanged() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();
            field.setText("half typed");

            press(field, KeyCode.UP);

            assertEquals("half typed", field.getText());
        });
    }

    @Test
    public void arrowKeys_browseSubmittedCommandsAndRestoreDraft() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();
            submit(field, "list");
            submit(field, "view 1");
            field.setText("half typed");

            press(field, KeyCode.UP);
            assertEquals("view 1", field.getText());
            assertEquals("view 1".length(), field.getCaretPosition());
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());

            press(field, KeyCode.DOWN);
            assertEquals("view 1", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("half typed", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("half typed", field.getText());
        });
    }

    @Test
    public void submit_success_clearsFieldAndIsRecalled() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();

            submit(field, "list");

            assertEquals("", field.getText());
            assertEquals(List.of("list"), executedCommands);
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
        });
    }

    @Test
    public void submit_failedCommand_isStillRecorded() throws Exception {
        runOnFxThread(() -> {
            TextField field = createFailingCommandField();

            submit(field, "bogus command");

            assertEquals("bogus command", field.getText());
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            field.setText("");
            press(field, KeyCode.UP);
            assertEquals("bogus command", field.getText());
        });
    }

    @Test
    public void submit_blankText_isNotRecorded() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();

            submit(field, "");
            press(field, KeyCode.UP);

            assertEquals("", field.getText());
            assertTrue(executedCommands.isEmpty());
        });
    }

    @Test
    public void editingRecalledText_doesNotChangeStoredEntry() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();
            submit(field, "list");

            press(field, KeyCode.UP);
            field.setText("list edited");
            press(field, KeyCode.DOWN);
            press(field, KeyCode.UP);

            assertEquals("list", field.getText());
        });
    }

    @Test
    public void submitRecalledText_followsNormalHistoryRules() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();
            submit(field, "list");
            submit(field, "view 1");

            press(field, KeyCode.UP);
            press(field, KeyCode.UP);
            submit(field, field.getText());

            assertEquals(List.of("list", "view 1", "list"), executedCommands);
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.UP);
            assertEquals("view 1", field.getText());
        });
    }

    @Test
    public void arrowKeys_withModifier_areNotIntercepted() throws Exception {
        runOnFxThread(() -> {
            TextField field = createCommandField();
            submit(field, "list");
            field.setText("half typed");

            for (boolean[] modifiers : new boolean[][] {
                {true, false, false, false}, {false, true, false, false},
                {false, false, true, false}, {false, false, false, true}}) {
                KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP,
                        modifiers[0], modifiers[1], modifiers[2], modifiers[3]);
                field.fireEvent(event);

                assertFalse(event.isConsumed());
                assertEquals("half typed", field.getText());
            }
        });
    }

    @Test
    public void arrowKeys_inOtherControl_doNotChangeCommandBox() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = createCommandBox(text -> new CommandResult("ok"));
            TextField commandField = (TextField) commandBox.getRoot().lookup("#commandTextField");
            TextField otherField = new TextField("other");
            VBox root = new VBox(commandBox.getRoot(), otherField);
            assertEquals(2, root.getChildren().size());
            submit(commandField, "list");
            commandField.setText("half typed");

            press(otherField, KeyCode.UP);

            assertEquals("half typed", commandField.getText());
            assertEquals("other", otherField.getText());
        });
    }

    private TextField createCommandField() {
        CommandBox commandBox = createCommandBox(text -> {
            executedCommands.add(text);
            return new CommandResult("ok");
        });
        return (TextField) commandBox.getRoot().lookup("#commandTextField");
    }

    private TextField createFailingCommandField() {
        CommandBox commandBox = createCommandBox(text -> {
            throw new ParseException("failed");
        });
        return (TextField) commandBox.getRoot().lookup("#commandTextField");
    }

    private CommandBox createCommandBox(CommandBox.CommandExecutor executor) {
        return new CommandBox(executor);
    }

    private static void submit(TextField field, String text) {
        field.setText(text);
        field.fireEvent(new ActionEvent());
    }

    private static void press(Node node, KeyCode code) {
        node.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
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
