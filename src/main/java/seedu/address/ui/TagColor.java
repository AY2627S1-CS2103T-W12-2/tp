package seedu.address.ui;

import javafx.scene.control.Label;
import seedu.address.model.tag.Tag;

/**
 * Chooses a stable palette color for a tag shown in the UI.
 */
final class TagColor {

    static final int PALETTE_SIZE = 6;

    private TagColor() {
    }

    /** Creates a tag label with the color chosen from its name. */
    static Label createLabel(Tag tag) {
        Label label = new Label(tag.tagName);
        label.getStyleClass().add(styleClassFor(tag));
        return label;
    }

    /** Returns the palette style class for the tag name normalized by {@link Tag}. */
    static String styleClassFor(Tag tag) {
        return "tag-color-" + Math.floorMod(tag.tagName.hashCode(), PALETTE_SIZE);
    }
}
