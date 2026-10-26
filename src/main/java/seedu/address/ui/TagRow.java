package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import seedu.address.model.tag.Tag;

/**
 * Fills a card's tag row with the tags of a contact, on one line only.
 * Tags are shown in name order while they fit the row's width; the rest are replaced by a {@code +N} label.
 */
final class TagRow {

    /** Style class of the {@code +N} label, so it can be styled apart from the tags. */
    static final String OVERFLOW_STYLE_CLASS = "tag-overflow";

    private static final double GAP = 7;

    private final HBox row;
    private final List<Label> tagLabels;
    private final Label overflowLabel = new Label();

    /**
     * Fills {@code row} with a label per tag (sorted by name) and a {@code +N} label, and keeps the row fitted to
     * its width from now on.
     */
    TagRow(HBox row, Set<Tag> tags) {
        this.row = row;
        this.tagLabels = tags.stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(TagRow::createTagLabel)
                .collect(Collectors.toList());
        overflowLabel.getStyleClass().add(OVERFLOW_STYLE_CLASS);
        row.setSpacing(GAP);
        // The row takes the width its parent gives it, rather than asking for room for every tag.
        row.setMinWidth(0);
        row.setPrefWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().addAll(tagLabels);
        row.getChildren().add(overflowLabel);
        fit();
        row.widthProperty().addListener((observable, oldWidth, newWidth) -> fit());
    }

    /** Creates the label of one tag. */
    private static Label createTagLabel(Tag tag) {
        return new Label(tag.tagName);
    }

    /**
     * Returns how many leading tags to show, given the width of each tag, the {@code gap} between labels, the
     * width of the {@code +N} label and the room available. All tags are shown if they fit. Otherwise the
     * largest number of tags that still leaves room for the {@code +N} label is shown, which may be zero.
     */
    static int visibleTagCount(List<Double> tagWidths, double gap, double overflowWidth, double availableWidth) {
        if (rowWidth(tagWidths, tagWidths.size(), gap) <= availableWidth) {
            return tagWidths.size();
        }
        for (int count = tagWidths.size() - 1; count > 0; count--) {
            if (rowWidth(tagWidths, count, gap) + gap + overflowWidth <= availableWidth) {
                return count;
            }
        }
        return 0;
    }

    /** Returns the width of the first {@code count} tags laid side by side. */
    private static double rowWidth(List<Double> tagWidths, int count, double gap) {
        double width = 0;
        for (int i = 0; i < count; i++) {
            width += tagWidths.get(i) + (i > 0 ? gap : 0);
        }
        return width;
    }

    /** Shows the tags that fit the row's current width and a {@code +N} label for the others. */
    private void fit() {
        // Hidden labels stay in the row (just unmanaged), so they keep their styling and can still be measured.
        List<Double> widths = new ArrayList<>();
        tagLabels.forEach(label -> widths.add(label.prefWidth(-1)));
        // The widest possible "+N" (every tag hidden), so it never needs re-measuring while the row narrows.
        overflowLabel.setText("+" + tagLabels.size());
        int visible = visibleTagCount(widths, row.getSpacing(), overflowLabel.prefWidth(-1), row.getWidth());
        for (int i = 0; i < tagLabels.size(); i++) {
            show(tagLabels.get(i), i < visible);
        }
        overflowLabel.setText("+" + (tagLabels.size() - visible));
        show(overflowLabel, visible < tagLabels.size());
    }

    private static void show(Label label, boolean isShown) {
        label.setVisible(isShown);
        label.setManaged(isShown);
    }
}
