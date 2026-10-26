package seedu.address.ui;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.PhotoStorage;

/**
 * Panel showing the details of the person being viewed.
 */
public class PersonDetailPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailPanel.fxml";
    private static final String EMPTY_FIELD_TEXT = "-";
    private static final String UNKNOWN_TIME_TEXT = "\u2014"; // em dash
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(Model.DISPLAY_ZONE);
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
    private Label followUp;
    @FXML
    private ImageView photo;
    @FXML
    private Label photoPlaceholder;
    @FXML
    private Label createdAt;
    @FXML
    private Label lastEditedAt;
    @FXML
    private Label lastViewedAt;

    /**
     * Creates a {@code PersonDetailPanel} that shows the person held by {@code viewedPerson},
     * and updates whenever that person changes.
     */
    public PersonDetailPanel(ObservableValue<Person> viewedPerson) {
        super(FXML);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
        showPerson(viewedPerson.getValue());
        viewedPerson.addListener((observable, oldPerson, newPerson) -> showPerson(newPerson));
    }

    /**
     * Scrolls the panel by the number of pixels in {@code event}. JavaFX's own handling moves the content by that
     * amount as a fraction of the whole content height, not of the part that can scroll, so it feels far too slow.
     */
    private void handleScroll(ScrollEvent event) {
        // the layout bounds are what ScrollPane itself uses to size its scroll range, so an effect or transform
        // on the content does not change the result
        OptionalDouble newVvalue = scrolledVvalue(scrollPane.getVvalue(), event.getDeltaY(),
                content.getLayoutBounds().getHeight(), scrollPane.getViewportBounds().getHeight(),
                scrollPane.getVmin(), scrollPane.getVmax());
        if (newVvalue.isPresent()) {
            scrollPane.setVvalue(newVvalue.getAsDouble());
            event.consume();
        }
    }

    /**
     * Returns the vertical scroll position after scrolling up by {@code deltaY} pixels (down if it is negative),
     * between {@code vmin} (top) and {@code vmax} (bottom), the range of the scroll pane's {@code vvalue}.
     * Returns an empty {@code OptionalDouble} if nothing should change: when {@code deltaY} is zero, or when the
     * content is not taller than the viewport so there is nothing to scroll.
     */
    static OptionalDouble scrolledVvalue(double vvalue, double deltaY, double contentHeight,
            double viewportHeight, double vmin, double vmax) {
        double scrollableHeight = contentHeight - viewportHeight;
        if (scrollableHeight <= 0 || deltaY == 0) {
            return OptionalDouble.empty();
        }
        double scrolled = vvalue - deltaY / scrollableHeight * (vmax - vmin);
        return OptionalDouble.of(Math.min(vmax, Math.max(vmin, scrolled)));
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
        followUp.setText(person.getFollowUpDate().map(Object::toString).orElse(EMPTY_FIELD_TEXT));
        createdAt.setText(formatTime(person.getActivity().getCreatedAt()));
        lastEditedAt.setText(formatTime(person.getActivity().getLastEditedAt()));
        lastViewedAt.setText(formatTime(person.getActivity().getLastViewedAt()));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(TagColor.createLabel(tag)));
    }

    /**
     * Formats {@code time} in Singapore time, or as an em dash if it is not known.
     */
    private static String formatTime(Optional<Instant> time) {
        return time.map(TIME_FORMAT::format).orElse(UNKNOWN_TIME_TEXT);
    }

    private static String orPlaceholder(String value) {
        return value.isEmpty() ? EMPTY_FIELD_TEXT : value;
    }
}
