package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.parser.AddressBookParser.MESSAGE_UNKNOWN_COMMAND_WITH_LIST;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class ResultDisplayTest {

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
    public void setFeedbackToUser_showsTheMessage() {
        ResultDisplay display = new ResultDisplay();

        display.setFeedbackToUser("Listed all persons.");

        assertEquals("Listed all persons.", textArea(display).getText());
    }

    @Test
    public void resultDisplay_wrapsLongMessagesInsteadOfScrollingSideways() {
        ResultDisplay display = new ResultDisplay();

        display.setFeedbackToUser(MESSAGE_UNKNOWN_COMMAND_WITH_LIST);

        // the unknown command message lists every command word, so it is much wider than the box
        assertTrue(textArea(display).isWrapText());
        assertEquals(MESSAGE_UNKNOWN_COMMAND_WITH_LIST, textArea(display).getText());
    }

    private static TextArea textArea(ResultDisplay display) {
        return (TextArea) display.getRoot().lookup("#resultDisplay");
    }
}
