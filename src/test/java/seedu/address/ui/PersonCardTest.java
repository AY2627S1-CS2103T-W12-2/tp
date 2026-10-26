package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_AMY;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
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
    public void constructor_manyTagsInNarrowCard_showsOneRowWithOverflowCount() {
        PersonCard card = laidOutCard(new PersonBuilder().withTags("fullstack-ml-eng-26", "mentor", "backend",
                "python", "react", "devops", "cloud").build(), 300);

        List<String> shown = shownTagNames(card);
        assertTrue(shown.size() < 7);
        assertEquals(List.of("backend", "cloud", "devops", "fullstack-ml-eng-26", "mentor", "python", "react")
                .subList(0, shown.size()), shown);
        assertEquals("+" + (7 - shown.size()), overflowLabel(card).getText());
        assertTrue(overflowLabel(card).isVisible());
        assertEquals(1, shownTagLabelsAndOverflow(card).stream().map(Label::getLayoutY).distinct().count());

        Pane tags = (Pane) card.getRoot().lookup("#tags");
        List<Label> shownTags = shownTagLabelsAndOverflow(card).stream()
                .filter(label -> !label.getStyleClass().contains(TagRow.OVERFLOW_STYLE_CLASS))
                .collect(Collectors.toList());
        assertTrue(shownTags.size() > 0);
        Label last = shownTags.get(shownTags.size() - 1);
        assertTrue(last.getBoundsInParent().getMaxX() <= tags.getWidth());
    }

    @Test
    public void constructor_fewShortTags_showsAllTagsWithoutOverflow() {
        PersonCard card = laidOutCard(new PersonBuilder().withTags("mentor", "ai").build(), 300);

        assertEquals(List.of("ai", "mentor"), shownTagNames(card));
        assertFalse(overflowLabel(card).isVisible());
        assertFalse(overflowLabel(card).isManaged());
    }

    @Test
    public void constructor_manyTags_doesNotGrowCardHeight() {
        PersonCard one = laidOutCard(new PersonBuilder().withTags("mentor").build(), 300);
        PersonCard many = laidOutCard(new PersonBuilder().withTags("fullstack-ml-eng-26", "mentor", "backend",
                "python", "react", "devops", "cloud").build(), 300);

        assertEquals(one.getRoot().prefHeight(300), many.getRoot().prefHeight(300));
    }

    @Test
    public void tagRow_cardResized_refitsTags() {
        PersonCard card = laidOutCard(new PersonBuilder().withTags("backend", "cloud", "devops", "mentor").build(),
                600);
        assertEquals(4, shownTagNames(card).size());

        resize(card, 120);

        assertTrue(shownTagNames(card).size() < 4);
        assertTrue(overflowLabel(card).isVisible());
    }

    private PersonCard laidOutCard(Person person, double width) {
        PersonCard card = new PersonCard(person, 1);
        Scene scene = new Scene(card.getRoot(), width, 200);
        layOut(scene);
        return card;
    }

    private void resize(PersonCard card, double width) {
        Scene scene = card.getRoot().getScene();
        card.getRoot().resize(width, 200);
        layOut(scene);
    }

    /** Applies CSS and lays out twice: the second pass lays out the tags refitted by the first. */
    private void layOut(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        scene.getRoot().layout();
    }

    private List<Label> shownTagLabelsAndOverflow(PersonCard card) {
        Pane tags = (Pane) card.getRoot().lookup("#tags");
        return tags.getChildren().stream().map(node -> (Label) node)
                .filter(label -> label.isVisible() && label.isManaged()).collect(Collectors.toList());
    }

    private List<String> shownTagNames(PersonCard card) {
        return shownTagLabelsAndOverflow(card).stream()
                .filter(label -> !label.getStyleClass().contains(TagRow.OVERFLOW_STYLE_CLASS))
                .map(Label::getText).collect(Collectors.toList());
    }

    private Label overflowLabel(PersonCard card) {
        return (Label) card.getRoot().lookup("." + TagRow.OVERFLOW_STYLE_CLASS);
    }

    private Label getStarLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#star");
    }

    private Label getLinkedInLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#linkedIn");
    }
}
