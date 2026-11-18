package com.team.telemetry.core.event;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

// Gabriella contributed this code: added the sequential time-stamped event factory
/** Factory pattern: the single place where time-stamped, sequentially numbered events are built. */
public class TelemetryEventFactory {
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Creates an event factory using the supplied clock.
     *
     * @param clock source of event timestamps
     * @throws NullPointerException if {@code clock} is {@code null}
     */
    public TelemetryEventFactory(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Creates an event with the next identifier and the current clock instant.
     *
     * @param type event category
     * @param data event attributes; copied by the event
     * @return the newly created event
     */
    public TelemetryEvent create(EventType type, Map<String, Object> data) {
        return new TelemetryEvent(sequence.incrementAndGet(), type, clock.instant(), data);
    }

    // Gabriella contributed this code: added the key/value shorthand for building event data
    /**
     * Creates an event from alternating attribute names and values.
     *
     * @param type event category
     * @param keyValues alternating key/value arguments
     * @return the newly created event
     * @throws IllegalArgumentException if {@code keyValues} has an odd number of elements
     */
    public TelemetryEvent of(EventType type, Object... keyValues) {
        return create(type, data(keyValues));
    }

    /**
     * Builds an insertion-ordered map from alternating attribute names and values.
     *
     * @param keyValues alternating key/value arguments
     * @return a new insertion-ordered map; later duplicate keys replace earlier values
     * @throws IllegalArgumentException if {@code keyValues} has an odd number of elements
     */
    public static Map<String, Object> data(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value arguments must come in pairs");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
}
