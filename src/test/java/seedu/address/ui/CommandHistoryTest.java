package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private final CommandHistory history = new CommandHistory();

    @Test
    public void older_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), history.older("draft"));
    }

    @Test
    public void newer_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), history.newer());
    }

    @Test
    public void newer_notBrowsing_returnsEmpty() {
        history.record("list");

        assertEquals(Optional.empty(), history.newer());
    }

    @Test
    public void record_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.record(null));
    }

    @Test
    public void record_blankCommands_areIgnored() {
        history.record("");
        history.record("   ");

        assertEquals(Optional.empty(), history.older(""));
    }

    @Test
    public void older_oneEntry_staysAtThatEntry() {
        history.record("list");

        assertEquals(Optional.of("list"), history.older(""));
        assertEquals(Optional.of("list"), history.older("list"));
    }

    @Test
    public void older_manyEntries_walksFromNewestToOldestAndStops() {
        history.record("list");
        history.record("view 1");
        history.record("tag 1 t/friend");

        assertEquals(Optional.of("tag 1 t/friend"), history.older(""));
        assertEquals(Optional.of("view 1"), history.older("tag 1 t/friend"));
        assertEquals(Optional.of("list"), history.older("view 1"));
        // already at the oldest entry
        assertEquals(Optional.of("list"), history.older("list"));
    }

    @Test
    public void newer_afterGoingOlder_walksBackToNewestThenRestoresDraft() {
        history.record("list");
        history.record("view 1");

        history.older("half typed");
        history.older("view 1");

        assertEquals(Optional.of("view 1"), history.newer());
        assertEquals(Optional.of("half typed"), history.newer());
        // browsing has ended, so there is nothing newer
        assertEquals(Optional.empty(), history.newer());
    }

    @Test
    public void older_draftIsCapturedOnlyWhenBrowsingStarts() {
        history.record("list");
        history.record("view 1");

        history.older("my draft");
        // the user edits the recalled text and goes up again: the edit must not replace the draft
        history.older("view 1 edited");

        history.newer();
        assertEquals(Optional.of("my draft"), history.newer());
    }

    @Test
    public void older_emptyDraft_isRestoredAsEmptyText() {
        history.record("list");

        history.older("");

        assertEquals(Optional.of(""), history.newer());
    }

    @Test
    public void record_consecutiveDuplicates_areCollapsed() {
        history.record("list");
        history.record("list");
        history.record("list");

        assertEquals(Optional.of("list"), history.older(""));
        // only one entry exists, so going older again stays on it
        assertEquals(Optional.of("list"), history.older("list"));
        assertEquals(Optional.of(""), history.newer());
    }

    @Test
    public void record_nonConsecutiveDuplicates_areKept() {
        history.record("list");
        history.record("view 1");
        history.record("list");

        assertEquals(Optional.of("list"), history.older(""));
        assertEquals(Optional.of("view 1"), history.older("list"));
        assertEquals(Optional.of("list"), history.older("view 1"));
    }

    @Test
    public void record_whileBrowsing_endsBrowsing() {
        history.record("list");
        history.record("view 1");
        history.older("draft");

        history.record("view 1");

        // browsing restarted: the newest entry is shown first, and there is no stale draft to restore
        assertEquals(Optional.of("view 1"), history.older("fresh draft"));
        assertEquals(Optional.of("fresh draft"), history.newer());
    }

    @Test
    public void record_exceedingLimit_evictsOldestEntries() {
        for (int i = 1; i <= CommandHistory.MAX_ENTRIES + 1; i++) {
            history.record("command " + i);
        }

        Optional<String> shown = history.older("");
        String last = null;
        int steps = 0;
        while (shown.isPresent() && !shown.get().equals(last)) {
            last = shown.get();
            steps++;
            shown = history.older(last);
        }

        assertEquals(CommandHistory.MAX_ENTRIES, steps);
        // command 1 was evicted, so the oldest remaining entry is command 2
        assertEquals("command 2", last);
    }

    @Test
    public void record_exactlyAtLimit_keepsEveryEntry() {
        for (int i = 1; i <= CommandHistory.MAX_ENTRIES; i++) {
            history.record("command " + i);
        }

        String last = null;
        Optional<String> shown = history.older("");
        while (shown.isPresent() && !shown.get().equals(last)) {
            last = shown.get();
            shown = history.older(last);
        }

        assertEquals("command 1", last);
        assertFalse(last.isEmpty());
    }
}
