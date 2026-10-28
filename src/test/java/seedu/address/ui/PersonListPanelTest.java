package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class PersonListPanelTest {
    @BeforeAll
    public static void startJavaFxToolkit() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // The toolkit was already started by another UI test.
        }
    }

    @Test
    public void statusLabel_filterThenViewAndList_staysBoundAndEnablesWrapping() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);
        PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(), model.getPersonListStatus());
        Label label = (Label) panel.getRoot().lookup("#personListStatus");
        AddressBookParser parser = new AddressBookParser();

        assertTrue(label.isWrapText());
        assertTrue(label.textProperty().isBound());
        parser.parseCommand("group t/friends").execute(model);
        assertEquals("Group: friends - Original order - 3 contact(s)", label.getText());
        parser.parseCommand("view 1").execute(model);
        assertEquals("Group: friends - Original order - 3 contact(s)", label.getText());
        parser.parseCommand("sort added").execute(model);
        assertEquals("Group: friends - Sorted by added - 3 contact(s)", label.getText());
        parser.parseCommand("filter starred").execute(model);
        assertEquals("Starred only - Sorted by added - 0 contact(s)", label.getText());
        parser.parseCommand("list").execute(model);
        assertEquals("Original order - 7 contact(s)", label.getText());
    }

    @Test
    public void statusLabel_longDueHeadingAtNarrowWidth_wrapsToMultipleLines() throws Exception {
        FutureTask<Void> checkWrapping = new FutureTask<>(() -> {
            Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), FIXED_CLOCK);
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(), model.getPersonListStatus());
            Label label = (Label) panel.getRoot().lookup("#personListStatus");
            new AddressBookParser().parseCommand("due").execute(model);

            // Attach a scene and apply CSS so the label has a skin before measuring its preferred height.
            new Scene(panel.getRoot(), 340, 480);
            panel.getRoot().applyCss();
            panel.getRoot().layout();

            assertTrue(label.getText().startsWith("Due on/before "));
            double unwrappedWidth = label.prefWidth(-1);
            double singleLineHeight = label.prefHeight(unwrappedWidth);
            assertTrue(label.prefHeight(unwrappedWidth / 2) > singleLineHeight,
                    "A narrow due heading should require more height than its unwrapped text");
            return null;
        });
        Platform.runLater(checkWrapping);
        checkWrapping.get(10, TimeUnit.SECONDS);
    }
}
