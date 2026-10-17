package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label linkedIn;
    @FXML
    private Label followUp;
    @FXML
    private Label overdue;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code PersonCard} that decides whether the follow-up date is overdue using the date on {@code clock}.
     */
    PersonCard(Person person, int displayedIndex, Clock clock) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        linkedIn.setText(person.getLinkedIn().value);
        linkedIn.setVisible(!person.getLinkedIn().isEmpty());
        linkedIn.setManaged(!person.getLinkedIn().isEmpty());
        boolean hasFollowUp = !person.getFollowUpDate().isEmpty();
        followUp.setText("Follow up: " + person.getFollowUpDate().value);
        followUp.setVisible(hasFollowUp);
        followUp.setManaged(hasFollowUp);
        boolean isOverdue = person.getFollowUpDate().isOverdueOn(LocalDate.now(clock));
        overdue.setVisible(isOverdue);
        overdue.setManaged(isOverdue);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
}
