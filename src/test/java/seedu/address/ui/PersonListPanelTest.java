package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
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
    public void statusLabel_filterThenViewAndList_staysBoundAndWraps() throws Exception {
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
}
