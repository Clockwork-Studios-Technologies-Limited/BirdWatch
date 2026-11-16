// Jibril contributed this code:

package com.team.telemetry.support;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for creating telemetry events at precise timestamps
 * for testing purposes.
 *
 * <p>Provides a convenient way to construct {@link TelemetryEvent} instances
 * relative to a fixed starting point defined by {@link MutableClock#T0}.
 * Each generated event receives a unique, incrementing identifier and
 * optionally includes additional event data.
 *
 * <p>Primarily intended for analyzer tests that require deterministic
 * event timestamps and predictable event sequences.
 */
public final class Events {
    
    /** Generates unique identifiers for created telemetry events. */
    private static final AtomicLong IDS = new AtomicLong();

    /**
     * Prevents instantiation of this utility class.
     */
    private Events() { }

    /**
     * Creates a telemetry event at a specified number of seconds relative
     * to the fixed starting timestamp.
     *
     * <p>The event timestamp is calculated by adding the specified offset
     * to {@link MutableClock#T0}. A unique identifier is generated for each
     * event, and the supplied key-value pairs are converted into event data
     * using {@link TelemetryEventFactory#data(Object...)}.
     *
     * @param secondsAfterStart the number of seconds after the fixed starting
     *                          timestamp at which the event should occur;
     *                          negative values represent timestamps before
     *                          the starting point
     * @param type the type of telemetry event to create
     * @param keyValues optional alternating keys and values representing
     *                  additional event data
     * @return a telemetry event with the generated identifier, specified type,
     *         calculated timestamp, and associated data
     */
    public static TelemetryEvent at(long secondsAfterStart, EventType type, Object... keyValues) {
        Instant ts = MutableClock.T0.plusSeconds(secondsAfterStart);
        return new TelemetryEvent(IDS.incrementAndGet(), type, ts, TelemetryEventFactory.data(keyValues));
    }
}
