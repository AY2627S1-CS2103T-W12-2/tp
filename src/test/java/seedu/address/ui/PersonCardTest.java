package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

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

    @Test
    public void constructor_sameTagHasSameColorClassAcrossCards() {
        Person person = new PersonBuilder().withTags("react").build();
        PersonCard firstCard = new PersonCard(person, 1);
        PersonCard secondCard = new PersonCard(person, 2);
        FlowPane firstTags = (FlowPane) firstCard.getRoot().lookup("#tags");
        FlowPane secondTags = (FlowPane) secondCard.getRoot().lookup("#tags");
        Label react = (Label) firstTags.getChildren().get(0);
        Label reactAgain = (Label) secondTags.getChildren().get(0);

        assertFalse(colorClass(react).isEmpty());
        assertEquals(colorClass(react), colorClass(reactAgain));
    }

    @Test
    public void constructor_tag_appliesPaletteBackground() {
        Person person = new PersonBuilder().withTags("react").build();
        PersonCard card = new PersonCard(person, 1);
        Scene scene = new Scene(card.getRoot());
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        card.getRoot().applyCss();
        FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
        Label react = (Label) tags.getChildren().get(0);

        assertNotNull(react.getBackground());
        assertFalse(react.getBackground().getFills().isEmpty());
    }

    @Test
    public void palette_everyColorClassHasBackground() {
        FlowPane tags = new FlowPane();
        tags.setId("tags");
        Scene scene = new Scene(tags);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

        for (int index = 0; index < TagColor.PALETTE_SIZE; index++) {
            Label label = new Label("tag");
            label.getStyleClass().add("tag-color-" + index);
            tags.getChildren().add(label);
        }
        tags.applyCss();

        for (int index = 0; index < TagColor.PALETTE_SIZE; index++) {
            Label label = (Label) tags.getChildren().get(index);
            assertNotNull(label.getBackground(), "Missing background for tag-color-" + index);
            assertFalse(label.getBackground().getFills().isEmpty());
        }
    }

    private String colorClass(Label tagLabel) {
        return tagLabel.getStyleClass().stream()
                .filter(styleClass -> styleClass.startsWith("tag-color-"))
                .findFirst()
                .orElse("");
    }

    private Label getStarLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#star");
    }

    private Label getLinkedInLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#linkedIn");
    }
}
