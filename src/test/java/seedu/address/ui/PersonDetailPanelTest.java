package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

    @Test
    public void constructor_meeting_showsFullContextAndBoundedCardCue() {
        String fullEvent = "x".repeat(80);
        var meeting = new seedu.address.model.person.Meeting(fullEvent, java.time.LocalDate.of(2026, 10, 20),
                "Mentor");
        Person person = new PersonBuilder().build().toBuilder().withMeeting(meeting).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));
        assertEquals(fullEvent, text(panel, "#event"));
        assertEquals("2026-10-20", text(panel, "#meetingDate"));
        assertEquals("Mentor", text(panel, "#role"));
        PersonCard card = new PersonCard(person, 1);
        assertEquals(32, ((Label) card.getRoot().lookup("#event")).getText().length());
        assertFalse(((ScrollPane) panel.getRoot().lookup(".scroll-pane")).isFitToHeight());
    }

    private Node lookup(PersonDetailPanel panel, String selector) {
        Node node = panel.getRoot().lookup(selector);
        if (node != null) {
            return node;
        }
        return ((ScrollPane) panel.getRoot().lookup(".scroll-pane")).getContent().lookup(selector);
    }

    private String text(PersonDetailPanel panel, String selector) {
        return ((Label) lookup(panel, selector)).getText();
    }
}
