package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.parser.AddressBookParser.MESSAGE_UNKNOWN_COMMAND_WITH_LIST;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class ResultDisplayTest {

    /** The minimum width of the main window, from {@code MainWindow.fxml}. */
    private static final double MINIMUM_WINDOW_WIDTH = 700;

    /** The height of the pane around the result box in {@code MainWindow.fxml}. */
    private static final double RESULT_PANE_HEIGHT = 100;

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

        assertEquals("Listed all persons.", getTextArea(display).getText());
    }

    @Test
    public void resultDisplay_wrapsLongMessagesInsteadOfScrollingSideways() {
        ResultDisplay display = new ResultDisplay();

        display.setFeedbackToUser(MESSAGE_UNKNOWN_COMMAND_WITH_LIST);

        // the unknown command message lists every command word, so it is much wider than the box
        assertTrue(getTextArea(display).isWrapText());
        assertEquals(MESSAGE_UNKNOWN_COMMAND_WITH_LIST, getTextArea(display).getText());
    }

    @Test
    public void resultDisplay_commandListFitsTheResultPaneAtTheMinimumWindowWidth() {
        ResultDisplay display = new ResultDisplay();
        // the same size and padding as the pane around the result box in MainWindow.fxml
        StackPane pane = new StackPane(display.getRoot());
        pane.setPadding(new Insets(5, 10, 5, 10));
        pane.setMinHeight(RESULT_PANE_HEIGHT);
        pane.setPrefHeight(RESULT_PANE_HEIGHT);
        pane.setMaxHeight(RESULT_PANE_HEIGHT);
        Scene scene = new Scene(pane, MINIMUM_WINDOW_WIDTH, RESULT_PANE_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

        display.setFeedbackToUser(MESSAGE_UNKNOWN_COMMAND_WITH_LIST);
        pane.applyCss();
        pane.layout();

        // the whole list can be read without scrolling in either direction
        assertTrue(getTextArea(display).isWrapText());
        for (Node node : pane.lookupAll(".scroll-bar")) {
            ScrollBar bar = (ScrollBar) node;
            assertFalse(bar.isVisible(), "unexpected " + bar.getOrientation() + " scroll bar");
        }
    }

    private static TextArea getTextArea(ResultDisplay display) {
        return (TextArea) display.getRoot().lookup("#resultDisplay");
    }
}
