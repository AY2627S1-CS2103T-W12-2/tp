package seedu.address.ui;

import java.util.Comparator;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.storage.PhotoStorage;

/**
 * Panel showing the details of the person being viewed.
 */
public class PersonDetailPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailPanel.fxml";
    private static final String EMPTY_FIELD_TEXT = "-";
    private static final Logger logger = LogsCenter.getLogger(PersonDetailPanel.class);

    @FXML
    private Label placeholder;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox content;
    @FXML
    private Label name;
    @FXML
    private Label star;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label linkedIn;
    @FXML
    private FlowPane tags;
    @FXML
    private Label note;
    @FXML
    private Label event;
    @FXML
    private Label meetingDate;
    @FXML
    private Label role;
    @FXML
    private ImageView photo;
    @FXML
    private Label photoPlaceholder;

    /**
     * Creates a {@code PersonDetailPanel} that shows the person held by {@code viewedPerson},
     * and updates whenever that person changes.
     */
    public PersonDetailPanel(ObservableValue<Person> viewedPerson) {
        super(FXML);
        showPerson(viewedPerson.getValue());
        viewedPerson.addListener((observable, oldPerson, newPerson) -> showPerson(newPerson));
    }

    /**
     * Shows the details of {@code person}, or the placeholder text if {@code person} is null.
     */
    private void showPerson(Person person) {
        boolean hasPerson = person != null;
        logger.fine("Detail panel shows " + (hasPerson ? "a contact" : "no contact"));
        placeholder.setVisible(!hasPerson);
        scrollPane.setVisible(hasPerson);
        content.setVisible(hasPerson);
        star.setVisible(hasPerson && person.isStarred());

        tags.getChildren().clear();
        if (!hasPerson) {
            ContactPhoto.cancel(photo);
            return;
        }

        ContactPhoto.show(person.getPhotoPath(), photo, photoPlaceholder, PhotoStorage.forApplication());
        name.setText(person.getName().fullName);
        phone.setText(orPlaceholder(person.getPhone().value));
        email.setText(orPlaceholder(person.getEmail().value));
        linkedIn.setText(orPlaceholder(person.getLinkedIn().value));
        note.setText(orPlaceholder(person.getNote().value));
        scrollPane.setVvalue(0);
        event.setText(person.getMeeting().getEventName().orElse(EMPTY_FIELD_TEXT));
        meetingDate.setText(person.getMeeting().getDate().map(Object::toString).orElse(EMPTY_FIELD_TEXT));
        role.setText(person.getMeeting().getRole().orElse(EMPTY_FIELD_TEXT));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    private static String orPlaceholder(String value) {
        return value.isEmpty() ? EMPTY_FIELD_TEXT : value;
    }
}
