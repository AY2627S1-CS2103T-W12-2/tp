package seedu.address.testutil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Fixed times for tests that record activity, so they do not depend on the real clock.
 */
public class TypicalTimes {

    /** The fixed time in whole seconds, as the model records it. */
    public static final Instant NOW = Instant.parse("2026-10-20T01:02:03Z");

    /** A clock stuck at {@link #NOW} plus 456 ms, to check that the model drops sub-second precision. */
    public static final Clock FIXED_CLOCK = Clock.fixed(NOW.plusMillis(456), ZoneId.of("Asia/Singapore"));

    /** The local date (in the zone of {@link #FIXED_CLOCK}) at {@link #FIXED_CLOCK}. */
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    public static final Instant EARLIER = Instant.parse("2026-10-17T04:12:33Z");
    public static final Instant EARLIEST = Instant.parse("2026-10-10T08:00:00Z");

    private TypicalTimes() {}
}
