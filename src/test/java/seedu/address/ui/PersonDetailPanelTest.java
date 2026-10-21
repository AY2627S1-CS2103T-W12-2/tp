package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;
import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Note;
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
    public void constructor_unknownActivity_showsEmDash() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));

        assertEquals("—", text(panel, "#createdAt"));
        assertEquals("—", text(panel, "#lastEditedAt"));
        assertEquals("—", text(panel, "#lastViewedAt"));
    }

    @Test
    public void constructor_activityTimes_shownInSingaporeTime() {
        // 20:00 UTC on the 17th is 04:00 on the 18th in Singapore (UTC+8)
        Person person = new PersonBuilder().withActivity(new Activity(Instant.parse("2026-10-17T20:00:00Z"),
                Instant.parse("2026-10-18T04:05:59Z"), Instant.parse("2026-12-31T16:00:00Z"))).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertEquals("2026-10-18 04:00", text(panel, "#createdAt"));
        assertEquals("2026-10-18 12:05", text(panel, "#lastEditedAt"));
        assertEquals("2027-01-01 00:00", text(panel, "#lastViewedAt"));
    }

    @Test
    public void constructor_partlyKnownActivity_showsDashOnlyForUnknown() {
        Person person = new PersonBuilder().withActivity(new Activity(null, Instant.parse("2026-10-18T00:00:00Z"),
                null)).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertEquals("—", text(panel, "#createdAt"));
        assertEquals("2026-10-18 08:00", text(panel, "#lastEditedAt"));
        assertEquals("—", text(panel, "#lastViewedAt"));
    }

    @Test
    public void viewedPersonChanges_activityTimesUpdate() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>();
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);

        viewedPerson.set(new PersonBuilder().build());
        assertEquals("—", text(panel, "#lastViewedAt"));

        // the same contact, now with a last viewed time (what the view command stores)
        viewedPerson.set(new PersonBuilder().withActivity(new Activity(null, null,
                Instant.parse("2026-10-20T01:02:03Z"))).build());
        assertEquals("2026-10-20 09:02", text(panel, "#lastViewedAt"));
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
    public void constructor_personWithNote_showsFullNote() {
        String text = "Met at the AI track; wants a React teammate (Sat 10am) - \"great\" ideas, a@b.com! #hack";
        Person person = new PersonBuilder().withNote(text).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertEquals(text, text(panel, "#note"));
    }

    @Test
    public void constructor_personWithoutNote_showsDash() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));

        assertEquals("-", text(panel, "#note"));
    }

    @Test
    public void viewedPersonNoteChanges_panelShowsNewNote() {
        Person person = new PersonBuilder().withNote("first note").build();
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>(person);
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);
        assertEquals("first note", text(panel, "#note"));

        viewedPerson.set(person.toBuilder().withNote(new Note("second note")).build());
        assertEquals("second note", text(panel, "#note"));

        viewedPerson.set(person.toBuilder().withNote(Note.EMPTY).build());
        assertEquals("-", text(panel, "#note"));
    }

    @Test
    public void constructor_veryLongNote_wrapsAndScrollsVerticallyOnly() {
        String longNote = ("word ".repeat(180)).strip();
        String unbrokenNote = "x".repeat(Note.MAX_LENGTH - longNote.length() - 1);
        Person person = new PersonBuilder().withNote(longNote + " " + unbrokenNote).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));
        Parent root = (Parent) panel.getRoot();
        new Scene(root, 480, 320);
        root.applyCss();
        root.layout();

        ScrollPane scrollPane = (ScrollPane) root.lookup("#scrollPane");
        Label note = (Label) root.lookup("#note");
        ScrollBar verticalBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");

        assertTrue(note.isWrapText());
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());
        // the note is wrapped to the width of the panel, so nothing overflows sideways
        assertTrue(note.getBoundsInParent().getMaxX() <= scrollPane.getViewportBounds().getWidth() + 1);
        // and the panel itself scrolls up and down to reach the end of the note
        assertTrue(verticalBar.isVisible());
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
        Parent root = (Parent) panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root);
        }
        // the scroll pane only adds its content to the scene graph once its skin exists
        root.applyCss();
        root.layout();
        return root.lookup(selector);
    }

    private String text(PersonDetailPanel panel, String selector) {
        return ((Label) lookup(panel, selector)).getText();
    }
}
