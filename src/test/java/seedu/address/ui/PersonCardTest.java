package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Meeting;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @Test
    public void constructor_eventCue_countsWholeCodePointsBeforeTruncating() {
        for (String text : new String[]{"😀".repeat(20), "a".repeat(30) + "😀" + "bc"}) {
            Person person = new PersonBuilder().build().toBuilder()
                    .withMeeting(new Meeting(text, null, null)).build();
            Label event = (Label) new PersonCard(person, 1).getRoot().lookup("#event");
            String expected = text.codePointCount(0, text.length()) > 32
                    ? "a".repeat(30) + "😀…" : text;
            assertEquals(expected, event.getText());
        }
    }

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
    public void constructor_personWithLinkedIn_showsLinkedIn() {
        Person person = new PersonBuilder().withLinkedIn(VALID_LINKEDIN_AMY).build();
        Label linkedIn = getLinkedInLabel(new PersonCard(person, 1));

        assertEquals(VALID_LINKEDIN_AMY, linkedIn.getText());
        assertTrue(linkedIn.isVisible());
        assertTrue(linkedIn.isManaged());
    }

    @Test
    public void constructor_personWithoutLinkedIn_hidesLinkedIn() {
        Person person = new PersonBuilder().withLinkedIn("").build();
        Label linkedIn = getLinkedInLabel(new PersonCard(person, 1));

        assertFalse(linkedIn.isVisible());
        assertFalse(linkedIn.isManaged());
    }

    @Test
    public void constructor_followUpDate_showsDateAndOverdueCueOnlyWhenBeforeToday() {
        LocalDate today = LocalDate.of(2026, 10, 21);
        Label none = followUp(new PersonCard(new PersonBuilder().build(), 1, today));
        Label future = followUp(new PersonCard(
                new PersonBuilder().withFollowUpDate(LocalDate.of(2026, 10, 22)).build(), 1, today));
        Label dueToday = followUp(new PersonCard(new PersonBuilder().withFollowUpDate(today).build(), 1, today));
        Label past = followUp(new PersonCard(
                new PersonBuilder().withFollowUpDate(LocalDate.of(2026, 10, 20)).build(), 1, today));

        assertFalse(none.isVisible());
        assertFalse(none.isManaged());
        assertEquals("Follow up: 2026-10-22", future.getText());
        assertEquals("Follow up: 2026-10-21", dueToday.getText());
        assertFalse(dueToday.getStyleClass().contains("overdue"));
        assertEquals("Follow up: 2026-10-20 (Overdue)", past.getText());
        assertTrue(past.getStyleClass().contains("overdue"));
    }

    private Label followUp(PersonCard card) {
        return (Label) card.getRoot().lookup("#followUp");
    }

    @Test
    public void constructor_starredPerson_showsStar() {
        Person person = new PersonBuilder().withStarred(true).build();
        Label star = getStarLabel(new PersonCard(person, 1));

        assertTrue(star.isVisible());
        assertTrue(star.isManaged());
    }

    @Test
    public void constructor_unstarredPerson_hidesStar() {
        Person person = new PersonBuilder().withStarred(false).build();
        Label star = getStarLabel(new PersonCard(person, 1));

        assertFalse(star.isVisible());
        assertFalse(star.isManaged());
    }

    private Label getStarLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#star");
    }

    private Label getLinkedInLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#linkedIn");
    }
}
