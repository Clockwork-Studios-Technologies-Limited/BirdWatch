// Jibril contributed this code: 

package com.team.telemetry.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A manually adjustable clock designed for deterministic testing.
 *
 * <p>Allows tests to control the progression of time without relying on the
 * system clock or introducing delays. This ensures that timestamps generated
 * during a test are predictable and reproducible.
 *
 * <p>The clock uses UTC as its time zone and provides a fixed default starting
 * timestamp through {@link #T0}. Tests can advance the clock by specifying
 * a duration or a number of seconds.
 *
 * <p>Extends {@link Clock} so it can be used wherever a standard Java clock
 * is expected.
 */
public class MutableClock extends Clock {

    /**
     * Default starting timestamp for test sessions:
     * January 1, 2026, at 10:00:00 UTC.
     */
    public static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    /** The current simulated timestamp maintained by this clock. */
    private Instant now;

    /**
     * Creates a mutable clock initialized to the default starting timestamp.
     *
     * @see #T0
     */
    public MutableClock() { this(T0); }

    /**
     * Creates a mutable clock initialized to a specified timestamp.
     *
     * @param start the initial timestamp of the clock
     */
    public MutableClock(Instant start) { this.now = start; }

    /**
     * Advances the current simulated time by the specified duration.
     *
     * <p>The clock's current timestamp is updated by adding the duration
     * to its existing value.
     *
     * @param d the duration by which to advance the clock
     */
    public void advance(Duration d) { now = now.plus(d); }

    /**
     * Advances the current simulated time by the specified number of seconds.
     *
     * <p>Delegates to {@link #advance(Duration)} using a duration expressed
     * in seconds.
     *
     * @param s the number of seconds by which to advance the clock
     */
    public void advanceSeconds(long s) { advance(Duration.ofSeconds(s)); }

    /**
     * Returns the time zone of this clock.
     *
     * @return {@link ZoneOffset#UTC}
     */
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }

    /**
     * Returns this clock with the requested time zone.
     *
     * <p>This implementation always returns the same clock because its
     * timestamps are maintained independently of time-zone configuration.
     *
     * @param zone the requested time zone
     * @return this mutable clock instance
     */
    @Override public Clock withZone(ZoneId zone) { return this; }

    /**
     * Returns the current simulated timestamp.
     *
     * @return the current instant maintained by this clock
     */
    @Override public Instant instant() { return now; }
}
