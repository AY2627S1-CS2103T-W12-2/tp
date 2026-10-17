package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ActivityTest {

    private static final Instant T1 = Instant.parse("2026-10-17T04:12:33Z");
    private static final Instant T2 = Instant.parse("2026-10-18T09:00:00Z");
    private static final Instant T3 = Instant.parse("2026-10-19T10:30:15Z");

    @Test
    public void empty_hasNoTimestamps() {
        assertEquals(Optional.empty(), Activity.EMPTY.getCreatedAt());
        assertEquals(Optional.empty(), Activity.EMPTY.getLastEditedAt());
        assertEquals(Optional.empty(), Activity.EMPTY.getLastViewedAt());
    }

    @Test
    public void constructor_nullTimestamps_treatedAsAbsent() {
        assertEquals(Activity.EMPTY, new Activity(null, null, null));
    }

    @Test
    public void getters_returnStoredValues() {
        Activity activity = new Activity(T1, T2, T3);
        assertEquals(Optional.of(T1), activity.getCreatedAt());
        assertEquals(Optional.of(T2), activity.getLastEditedAt());
        assertEquals(Optional.of(T3), activity.getLastViewedAt());
    }

    @Test
    public void withers_replaceOnlyTheirOwnField() {
        Activity activity = new Activity(T1, T2, T3);

        assertEquals(new Activity(T3, T2, T3), activity.withCreatedAt(T3));
        assertEquals(new Activity(T1, T3, T3), activity.withLastEditedAt(T3));
        assertEquals(new Activity(T1, T2, T1), activity.withLastViewedAt(T1));
    }

    @Test
    public void withers_doNotMutateOriginal() {
        Activity original = new Activity(T1, T2, T3);
        original.withCreatedAt(T3);
        assertEquals(new Activity(T1, T2, T3), original);
    }

    @Test
    public void equals() {
        Activity activity = new Activity(T1, T2, T3);

        // same values -> true
        assertTrue(activity.equals(new Activity(T1, T2, T3)));

        // same object -> true
        assertTrue(activity.equals(activity));

        // null -> false
        assertFalse(activity.equals(null));

        // different type -> false
        assertFalse(activity.equals(5));

        // each field differing -> false
        assertFalse(activity.equals(new Activity(T2, T2, T3)));
        assertFalse(activity.equals(new Activity(T1, T1, T3)));
        assertFalse(activity.equals(new Activity(T1, T2, T1)));

        // present vs absent -> false
        assertFalse(activity.equals(new Activity(T1, T2, null)));
        assertFalse(Activity.EMPTY.equals(activity));
    }

    @Test
    public void hashCode_equalActivities_sameHash() {
        assertEquals(new Activity(T1, T2, T3).hashCode(), new Activity(T1, T2, T3).hashCode());
        assertEquals(Activity.EMPTY.hashCode(), new Activity(null, null, null).hashCode());
    }
}
