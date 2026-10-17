package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Remembers the commands submitted during the current session, so that the user can browse back through them.
 * The history is kept in memory only. It keeps the latest {@value #MAX_ENTRIES} entries and stores a command
 * once if it is submitted several times in a row.
 */
public class CommandHistory {

    public static final int MAX_ENTRIES = 50;

    private final List<String> entries = new ArrayList<>();

    /** The index of the entry being shown, or {@code entries.size()} if the user is not browsing the history. */
    private int position = 0;

    /** The text that the user had typed when browsing started, restored when browsing ends. */
    private String draft = "";

    /**
     * Records {@code commandText} as the newest entry and ends any browsing in progress.
     * Blank commands are ignored, and a command that equals the newest entry is not stored twice.
     * The oldest entry is dropped when there are more than {@value #MAX_ENTRIES} entries.
     */
    public void record(String commandText) {
        requireNonNull(commandText);
        if (commandText.isBlank()) {
            return;
        }

        if (entries.isEmpty() || !entries.get(entries.size() - 1).equals(commandText)) {
            entries.add(commandText);
        }
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
        endBrowsing();
    }

    /**
     * Returns the next older entry, or the oldest entry if the user is already there.
     * If this starts a browse, {@code currentText} is kept as the draft to restore later.
     * Returns an empty {@code Optional} if there are no entries.
     */
    public Optional<String> older(String currentText) {
        requireNonNull(currentText);
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        if (!isBrowsing()) {
            draft = currentText;
            position = entries.size() - 1;
        } else if (position > 0) {
            position--;
        }
        return Optional.of(entryAtPosition());
    }

    /**
     * Returns the next newer entry. After the newest entry, returns the draft that the user had typed before
     * browsing started and ends the browse. Returns an empty {@code Optional} if the user is not browsing.
     */
    public Optional<String> newer() {
        if (!isBrowsing()) {
            return Optional.empty();
        }

        if (position < entries.size() - 1) {
            position++;
            return Optional.of(entryAtPosition());
        }

        String restoredDraft = draft;
        endBrowsing();
        return Optional.of(restoredDraft);
    }

    private boolean isBrowsing() {
        return position < entries.size();
    }

    private void endBrowsing() {
        position = entries.size();
        draft = "";
    }

    private String entryAtPosition() {
        assert isBrowsing();
        return entries.get(position);
    }
}
