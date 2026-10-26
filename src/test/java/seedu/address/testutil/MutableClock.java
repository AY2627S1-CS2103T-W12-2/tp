package seedu.address.testutil;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A {@link Clock} whose instant a test can move forward, so that successive model calls see different times.
 */
public class MutableClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    /**
     * Creates a clock that starts at {@code instant} and reports times in {@code zone}.
     */
    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    /**
     * Moves this clock forward by {@code millis} milliseconds.
     */
    public void advanceMillis(long millis) {
        instant = instant.plusMillis(millis);
    }

    /**
     * Moves this clock forward by {@code seconds} seconds.
     */
    public void advanceSeconds(long seconds) {
        instant = instant.plusSeconds(seconds);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
