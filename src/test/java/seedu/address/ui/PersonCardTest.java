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
import javafx.scene.control.Label;
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

    // The fixed clock says today is 2026-10-20.

    @Test
    public void constructor_futureFollowUpDate_showsDateWithoutOverdue() {
        Person person = new PersonBuilder().withFollowUpDate("2026-10-25").build();
        PersonCard card = new PersonCard(person, 1, FIXED_CLOCK);

        assertEquals("Follow up: 2026-10-25", label(card, "#followUp").getText());
        assertTrue(label(card, "#followUp").isVisible());
        assertFalse(label(card, "#overdue").isVisible());
        assertFalse(label(card, "#overdue").isManaged());
    }

    @Test
    public void constructor_followUpDueToday_isNotOverdue() {
        Person person = new PersonBuilder().withFollowUpDate("2026-10-20").build();
        PersonCard card = new PersonCard(person, 1, FIXED_CLOCK);

        assertEquals("Follow up: 2026-10-20", label(card, "#followUp").getText());
        assertFalse(label(card, "#overdue").isVisible());
    }

    @Test
    public void constructor_pastFollowUpDate_showsOverdue() {
        Person person = new PersonBuilder().withFollowUpDate("2026-10-19").build();
        PersonCard card = new PersonCard(person, 1, FIXED_CLOCK);

        assertEquals("Follow up: 2026-10-19", label(card, "#followUp").getText());
        assertTrue(label(card, "#overdue").isVisible());
        assertTrue(label(card, "#overdue").isManaged());
        assertEquals("OVERDUE", label(card, "#overdue").getText());
    }

    @Test
    public void constructor_noFollowUpDate_hidesFollowUpAndOverdue() {
        PersonCard card = new PersonCard(new PersonBuilder().build(), 1, FIXED_CLOCK);

        assertFalse(label(card, "#followUp").isVisible());
        assertFalse(label(card, "#followUp").isManaged());
        assertFalse(label(card, "#overdue").isVisible());
    }

    private Label label(PersonCard personCard, String selector) {
        return (Label) personCard.getRoot().lookup(selector);
    }

    private Label getLinkedInLabel(PersonCard personCard) {
        return (Label) personCard.getRoot().lookup("#linkedIn");
    }
}
