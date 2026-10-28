package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.storage.PhotoStorage;
import seedu.address.testutil.PersonBuilder;

public class PersonDetailPanelTest {

    /** A detail pane of this width has room for the photo beside the name. */
    private static final double WIDE_PANE_WIDTH = 700;

    /** The detail pane is about this wide when the main window has its minimum width of 700 pixels. */
    private static final double NARROW_PANE_WIDTH = 360;

    /** The longest name that the app allows is 50 characters. */
    private static final String LONG_NAME = "Alexandria Montgomery Wolfeschlegelstein Jr";

    @TempDir
    private Path photoFolder;

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
    public void constructor_tag_usesSameColorClassAsContactList() {
        Person person = new PersonBuilder().withTags("React").build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));
        PersonCard card = new PersonCard(person, 1);
        Label detailTag = (Label) ((FlowPane) lookup(panel, "#tags")).getChildren().get(0);
        Label listTag = (Label) ((FlowPane) card.getRoot().lookup("#tags")).getChildren().get(0);

        assertTrue(detailTag.getStyleClass().stream().anyMatch(styleClass -> styleClass.startsWith("tag-color-")));
        assertEquals(listTag.getStyleClass(), detailTag.getStyleClass());
    }

    @Test
    public void star_visibleOnlyForStarredViewedPerson() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>();
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);
        assertFalse(lookup(panel, "#star").isVisible());

        viewedPerson.set(new PersonBuilder().withStarred(true).build());
        assertTrue(lookup(panel, "#star").isVisible());

        viewedPerson.set(new PersonBuilder().withStarred(false).build());
        assertFalse(lookup(panel, "#star").isVisible());

        viewedPerson.set(new PersonBuilder().withStarred(true).build());
        viewedPerson.set(null);
        assertFalse(lookup(panel, "#star").isVisible());
    }

    @Test
    public void constructor_personWithoutTags_showsDashInPlaceOfTheTagRow() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));

        Label noTags = (Label) lookup(panel, "#noTags");
        assertEquals("-", noTags.getText());
        assertTrue(noTags.isVisible());
        assertTrue(noTags.isManaged());
        // the tag row would style the dash like a tag, and would leave an empty gap, so it is hidden
        FlowPane tags = (FlowPane) lookup(panel, "#tags");
        assertFalse(tags.isVisible());
        assertFalse(tags.isManaged());
    }

    @Test
    public void viewedPersonChanges_dashAndTagRowTakeTurns() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>(new PersonBuilder().withTags("react")
                .build());
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);
        FlowPane tags = (FlowPane) lookup(panel, "#tags");
        Label noTags = (Label) lookup(panel, "#noTags");

        // a contact with tags shows them and no dash
        assertTrue(tags.isVisible());
        assertTrue(tags.isManaged());
        assertFalse(noTags.isVisible());
        assertFalse(noTags.isManaged());

        // one without tags shows the dash and no empty row
        viewedPerson.set(new PersonBuilder().withName("No Tags Person").build());
        assertEquals(0, tags.getChildren().size());
        assertFalse(tags.isVisible());
        assertTrue(noTags.isVisible());

        // and back again
        viewedPerson.set(new PersonBuilder().withName("Tagged Person").withTags("figma", "ui-ux").build());
        assertEquals(2, tags.getChildren().size());
        assertTrue(tags.isVisible());
        assertFalse(noTags.isVisible());
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
    public void constructor_singaporeMidnightAndLeapDay_showsSingaporeDateAndMinute() {
        // 15:59:59 UTC is 23:59 in Singapore; one second later it is 00:00 on the next Singapore date
        Person person = new PersonBuilder().withActivity(new Activity(Instant.parse("2026-10-20T15:59:59Z"),
                Instant.parse("2026-10-20T16:00:00Z"), Instant.parse("2028-02-28T16:00:00Z"))).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));

        assertEquals("2026-10-20 23:59", text(panel, "#createdAt"));
        assertEquals("2026-10-21 00:00", text(panel, "#lastEditedAt"));
        assertEquals("2028-02-29 00:00", text(panel, "#lastViewedAt"));
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
        Parent root = (Parent) longNotePanelInScene().getRoot();

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
    public void photo_widePane_isBesideTheNameInAFixedFrameWithAndWithoutAPhoto() throws Exception {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>(new PersonBuilder().build());
        Parent root = (Parent) panelInScene(viewedPerson, WIDE_PANE_WIDTH).getRoot();
        ImageView photo = (ImageView) root.lookup("#photo");
        Label placeholder = (Label) root.lookup("#photoPlaceholder");

        // without a photo, the placeholder fills the frame
        assertTrue(placeholder.isVisible());
        assertFalse(photo.isVisible());
        assertFrameIsFixedAndBesideTheName(root);

        // a landscape and a portrait photo, loaded as in the app, leave the frame as it was
        for (String file : new String[] {"landscape.png", "portrait.png"}) {
            int width = file.startsWith("landscape") ? 400 : 300;
            int height = file.startsWith("landscape") ? 300 : 400;
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png",
                    photoFolder.resolve(file).toFile());
            ContactPhoto.show(file, photo, placeholder, new PhotoStorage(photoFolder)).get(10, TimeUnit.SECONDS);
            root.applyCss();
            root.layout();

            assertTrue(photo.isVisible(), file + " should be shown");
            assertFalse(placeholder.isVisible());
            assertFrameIsFixedAndBesideTheName(root);
        }

        // a contact with a very long name (the most the app allows is 50 characters) keeps the frame the same size
        viewedPerson.set(new PersonBuilder().withName(LONG_NAME).build());
        root.applyCss();
        root.layout();
        assertFrameIsFixedAndBesideTheName(root);
    }

    @Test
    public void photo_narrowPane_isBelowTheNameSoThatTheNameStaysReadable() {
        ObjectProperty<Person> viewedPerson = new SimpleObjectProperty<>(new PersonBuilder().withName(LONG_NAME)
                .build());
        Parent root = (Parent) panelInScene(viewedPerson, NARROW_PANE_WIDTH).getRoot();
        Bounds name = sceneBounds(root.lookup("#name"));
        Bounds frame = sceneBounds(root.lookup("#photoFrame"));

        // below the name instead of squeezing the name into a narrow column beside it
        assertTrue(frame.getMinY() >= name.getMaxY() - 1, "the photo should be below the name");
        assertTrue(name.getWidth() > 200, "the name is only " + name.getWidth() + " pixels wide");
        assertTrue(name.getHeight() < 110, "the name is " + name.getHeight() + " pixels tall");
        assertEquals(160, frame.getWidth(), 0.5);
        assertEquals(120, frame.getHeight(), 0.5);
    }

    /** Returns a panel showing {@code viewedPerson} in a window of {@code width} pixels, after layout. */
    private PersonDetailPanel panelInScene(ObjectProperty<Person> viewedPerson, double width) {
        PersonDetailPanel panel = new PersonDetailPanel(viewedPerson);
        Parent root = (Parent) panel.getRoot();
        new Scene(root, width, 600);
        // the second pass lays out the header again after it has been arranged for its width
        for (int pass = 0; pass < 2; pass++) {
            root.applyCss();
            root.layout();
        }
        return panel;
    }

    /** Checks that the photo frame is 160x120, to the right of the name and level with the top of it. */
    private void assertFrameIsFixedAndBesideTheName(Parent root) {
        Bounds name = sceneBounds(root.lookup("#name"));
        Bounds frame = sceneBounds(root.lookup("#photoFrame"));
        assertEquals(160, frame.getWidth(), 0.5);
        assertEquals(120, frame.getHeight(), 0.5);
        assertTrue(frame.getMinX() >= name.getMaxX() - 1, "the photo should be beside the name");
        assertEquals(name.getMinY(), frame.getMinY(), 1);
    }

    private static Bounds sceneBounds(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    @Test
    public void scroll_longContent_movesByThePixelsOfTheEvent() {
        Parent root = (Parent) longNotePanelInScene().getRoot();
        ScrollPane scrollPane = (ScrollPane) root.lookup("#scrollPane");
        double scrollableHeight = root.lookup("#content").getLayoutBounds().getHeight()
                - scrollPane.getViewportBounds().getHeight();
        assertTrue(scrollableHeight > 0);

        // an event that the panel handles itself does not bubble up to the parent any more
        boolean[] reachedParent = {false};
        root.addEventHandler(ScrollEvent.SCROLL, event -> reachedParent[0] = true);

        Event.fireEvent(scrollPane, scrollEvent(-40));

        assertEquals(40 / scrollableHeight, scrollPane.getVvalue(), 1e-6);
        assertFalse(reachedParent[0]);

        Event.fireEvent(scrollPane, scrollEvent(40));
        assertEquals(0, scrollPane.getVvalue(), 1e-6);
    }

    @Test
    public void scroll_contentThatFits_doesNotMovePanel() {
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build()));
        Parent root = (Parent) panel.getRoot();
        new Scene(root, 480, 2000);
        root.applyCss();
        root.layout();
        ScrollPane scrollPane = (ScrollPane) root.lookup("#scrollPane");

        // the panel leaves the event alone, so it keeps bubbling up to the parent
        boolean[] reachedParent = {false};
        root.addEventHandler(ScrollEvent.SCROLL, event -> reachedParent[0] = true);

        Event.fireEvent(scrollPane, scrollEvent(-40));

        assertEquals(0, scrollPane.getVvalue(), 1e-6);
        assertTrue(reachedParent[0]);
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

    @Test
    public void constructor_followUpDate_showsFullDateOrPlaceholder() {
        Person with = new PersonBuilder().withFollowUpDate(java.time.LocalDate.of(2026, 10, 25)).build();

        assertEquals("2026-10-25", text(new PersonDetailPanel(new SimpleObjectProperty<>(with)), "#followUp"));
        assertEquals("-", text(new PersonDetailPanel(new SimpleObjectProperty<>(new PersonBuilder().build())),
                "#followUp"));
    }

    /** Returns a panel showing a contact with a very long note, in a small window so that the panel scrolls. */
    private PersonDetailPanel longNotePanelInScene() {
        String longNote = ("word ".repeat(180)).strip();
        String unbrokenNote = "x".repeat(Note.MAX_LENGTH - longNote.length() - 1);
        Person person = new PersonBuilder().withNote(longNote + " " + unbrokenNote).build();
        PersonDetailPanel panel = new PersonDetailPanel(new SimpleObjectProperty<>(person));
        Parent root = (Parent) panel.getRoot();
        new Scene(root, 480, 320);
        root.applyCss();
        root.layout();
        return panel;
    }

    /** Returns a vertical scroll event of {@code deltaY} pixels (negative scrolls down), like one wheel notch. */
    private static ScrollEvent scrollEvent(double deltaY) {
        return new ScrollEvent(ScrollEvent.SCROLL, 10, 10, 10, 10, false, false, false, false, false, false,
                0, deltaY, 0, deltaY, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null);
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
