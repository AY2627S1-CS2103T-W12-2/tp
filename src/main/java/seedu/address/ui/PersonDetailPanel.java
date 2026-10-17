package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * Panel showing the details of the person being viewed.
 */
public class PersonDetailPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailPanel.fxml";
    private static final String EMPTY_FIELD_TEXT = "-";

    @FXML
    private Label placeholder;
    @FXML
    private VBox content;
    @FXML
    private Label name;
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

    private final Clock clock;

    /**
     * Creates a {@code PersonDetailPanel} that shows the person held by {@code viewedPerson},
     * and updates whenever that person changes.
     */
    public PersonDetailPanel(ObservableValue<Person> viewedPerson) {
        this(viewedPerson, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code PersonDetailPanel} that decides whether the follow-up date is overdue using the date on
     * {@code clock}.
     */
    PersonDetailPanel(ObservableValue<Person> viewedPerson, Clock clock) {
        super(FXML);
        this.clock = clock;
        showPerson(viewedPerson.getValue());
        viewedPerson.addListener((observable, oldPerson, newPerson) -> showPerson(newPerson));
    }

    /**
     * Shows the details of {@code person}, or the placeholder text if {@code person} is null.
     */
    private void showPerson(Person person) {
        boolean hasPerson = person != null;
        placeholder.setVisible(!hasPerson);
        content.setVisible(hasPerson);

        tags.getChildren().clear();
        if (!hasPerson) {
            return;
        }

        name.setText(person.getName().fullName);
        phone.setText(orPlaceholder(person.getPhone().value));
        email.setText(orPlaceholder(person.getEmail().value));
        linkedIn.setText(orPlaceholder(person.getLinkedIn().value));
        followUp.setText(orPlaceholder(person.getFollowUpDate().value));
        boolean isOverdue = person.getFollowUpDate().isOverdueOn(LocalDate.now(clock));
        overdue.setVisible(isOverdue);
        overdue.setManaged(isOverdue);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    private static String orPlaceholder(String value) {
        return value.isEmpty() ? EMPTY_FIELD_TEXT : value;
    }
}
