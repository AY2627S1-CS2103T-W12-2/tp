package seedu.address.ui;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.PhotoStorage;

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
    private FlowPane tags;
    @FXML
    private Label event;
    @FXML
    private Label followUp;
    @FXML
    private ImageView photo;
    @FXML
    private Label photoPlaceholder;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, LocalDate.now(Model.DISPLAY_ZONE));
    }

    /**
     * Creates a {@code PersonCard} that decides whether the follow-up is overdue as of {@code today}.
     */
    PersonCard(Person person, int displayedIndex, LocalDate today) {
        super(FXML);
        this.person = person;
        ContactPhoto.show(person.getPhotoPath(), photo, photoPlaceholder, PhotoStorage.forApplication());
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        linkedIn.setText(person.getLinkedIn().value);
        linkedIn.setVisible(!person.getLinkedIn().isEmpty());
        linkedIn.setManaged(!person.getLinkedIn().isEmpty());
        String eventName = person.getMeeting().getEventName().orElse("");
        event.setText(eventName.length() > 32 ? eventName.substring(0, 31) + "…" : eventName);
        event.setVisible(!eventName.isEmpty());
        event.setManaged(!eventName.isEmpty());
        showFollowUp(person, today);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
    /** Shows the follow-up date, with an Overdue cue if it is before {@code today}; hides the label if none. */
    private void showFollowUp(Person person, LocalDate today) {
        Optional<LocalDate> date = person.getFollowUpDate();
        boolean overdue = person.isFollowUpOverdueOn(today);
        followUp.setText(date.map(d -> "Follow up: " + d + (overdue ? " (Overdue)" : "")).orElse(""));
        followUp.setVisible(date.isPresent());
        followUp.setManaged(date.isPresent());
        if (overdue) {
            followUp.getStyleClass().add("overdue");
        }
    }

    /** Releases pending image work when a list cell discards this card. */
    void dispose() {
        ContactPhoto.cancel(photo);
    }

}
