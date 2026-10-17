package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory commandHistory = new CommandHistory();

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryKey);
    }

    /**
     * Recalls older or newer submitted commands when the Up or Down arrow is pressed on its own in the command box.
     * Key presses with a modifier key, and presses in other controls, are left alone.
     */
    private void handleHistoryKey(KeyEvent keyEvent) {
        if (hasModifier(keyEvent)) {
            return;
        }

        if (keyEvent.getCode() == KeyCode.UP) {
            commandHistory.older(commandTextField.getText()).ifPresent(this::showRecalledText);
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            commandHistory.newer().ifPresent(this::showRecalledText);
            keyEvent.consume();
        }
    }

    private static boolean hasModifier(KeyEvent keyEvent) {
        return keyEvent.isShiftDown() || keyEvent.isControlDown() || keyEvent.isAltDown() || keyEvent.isMetaDown();
    }

    /**
     * Shows {@code text} in the command box with the caret at the end, ready to be edited or submitted.
     */
    private void showRecalledText(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        commandHistory.record(commandText);
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
