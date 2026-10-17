package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonDetailPanelTest {

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
    public void constructor_noViewedPerson_showsPlaceholder() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>());

        assertTrue(lookup(panel, "#placeholder").isVisible());
        assertFalse(lookup(panel, "#content").isVisible());
    }

    @Test
    public void constructor_viewedPerson_showsAllFields() {
        Person person = new PersonBuilder().withName("Alex Yeo").withPhone("87654321")
                .withEmail("alex.yeo@example.com").withLinkedIn(VALID_LINKEDIN_AMY)
                .withTags("react", "backend").build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertFalse(lookup(panel, "#placeholder").isVisible());
        assertTrue(lookup(panel, "#content").isVisible());
        assertEquals("Alex Yeo", text(panel, "#name"));
        assertEquals("87654321", text(panel, "#phone"));
        assertEquals("alex.yeo@example.com", text(panel, "#email"));
        assertEquals(VALID_LINKEDIN_AMY, text(panel, "#linkedIn"));
        assertEquals(2, ((FlowPane) lookup(panel, "#tags")).getChildren().size());
    }

    @Test
    public void constructor_personWithoutLinkedIn_showsDash() {
        Person person = new PersonBuilder().withLinkedIn("").build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertEquals("-", text(panel, "#linkedIn"));
    }

    // The fixed clock says today is 2026-10-20.

    @Test
    public void constructor_futureFollowUpDate_showsDateWithoutOverdue() {
        Person person = new PersonBuilder().withFollowUpDate("2026-10-25").build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person), FIXED_CLOCK);

        assertEquals("2026-10-25", text(panel, "#followUp"));
        assertFalse(lookup(panel, "#overdue").isVisible());
    }

    @Test
    public void constructor_pastFollowUpDate_showsOverdue() {
        Person person = new PersonBuilder().withFollowUpDate("2026-10-19").build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person), FIXED_CLOCK);

        assertEquals("2026-10-19", text(panel, "#followUp"));
        assertTrue(lookup(panel, "#overdue").isVisible());
        assertEquals("OVERDUE", text(panel, "#overdue"));
    }

    @Test
    public void constructor_noFollowUpDate_showsDashAndNoOverdue() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()),
                FIXED_CLOCK);

        assertEquals("-", text(panel, "#followUp"));
        assertFalse(lookup(panel, "#overdue").isVisible());
    }

    @Test
    public void viewedPersonChanges_overdueIndicatorUpdates() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>();
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson, FIXED_CLOCK);

        viewedPerson.set(new PersonBuilder().withFollowUpDate("2026-10-19").build());
        assertTrue(lookup(panel, "#overdue").isVisible());

        viewedPerson.set(new PersonBuilder().withFollowUpDate("2026-10-25").build());
        assertFalse(lookup(panel, "#overdue").isVisible());
    }

    @Test
    public void viewedPersonChanges_panelUpdates() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>();
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);

        viewedPerson.set(new PersonBuilder().withName("Alex Yeo").withTags("react").build());
        assertTrue(lookup(panel, "#content").isVisible());
        assertEquals("Alex Yeo", text(panel, "#name"));

        viewedPerson.set(new PersonBuilder().withName("Priya Nair").withTags("figma", "ui-ux").build());
        assertEquals("Priya Nair", text(panel, "#name"));
        assertEquals(2, ((FlowPane) lookup(panel, "#tags")).getChildren().size());

        viewedPerson.set(null);
        assertTrue(lookup(panel, "#placeholder").isVisible());
        assertFalse(lookup(panel, "#content").isVisible());
    }

    private Node lookup(PersonDetailPanel panel, String selector) {
        return panel.getRoot().lookup(selector);
    }

    private String text(PersonDetailPanel panel, String selector) {
        return ((Label) lookup(panel, selector)).getText();
    }
}
