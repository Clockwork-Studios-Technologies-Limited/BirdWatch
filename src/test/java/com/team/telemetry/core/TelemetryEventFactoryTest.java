// Jibril contributed this code: Test cases for TelemetryEventFactory

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.support.MutableClock;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TelemetryEventFactory} and {@link TelemetryEvent}.
 *
 * <p>Verifies event creation, sequential ID assignment, timestamp generation,
 * data map ordering and immutability, typed data accessors, background event
 * detection, and event equality.
 */
class TelemetryEventFactoryTest {

    /**
     * Tests that generated telemetry events receive sequential identifiers
     * and timestamps based on the configured clock.
     *
     * <p>Verifies that advancing the clock changes the timestamp of
     * subsequently created events without affecting previous events.
     */
    @Test
    void numbersEventsSequentiallyAndStampsFromClock() {
        MutableClock clock = new MutableClock();
        TelemetryEventFactory factory = new TelemetryEventFactory(clock);

        TelemetryEvent first = factory.of(EventType.EDIT, "className", "A");
        clock.advanceSeconds(5);
        TelemetryEvent second = factory.of(EventType.EDIT, "className", "B");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(MutableClock.T0, first.getTimestamp());
        assertEquals(MutableClock.T0.plusSeconds(5), second.getTimestamp());
    }

    /**
     * Tests that telemetry event data preserves the insertion order
     * of key-value pairs and cannot be modified after creation.
     *
     * <p>Verifies that attempts to modify the data map throw
     * an {@link UnsupportedOperationException}.
     */
    @Test
    void dataMapKeepsInsertionOrderAndIsImmutable() {
        TelemetryEvent e = new TelemetryEventFactory(new MutableClock()).of(EventType.EDIT, "z", 1, "a", 2);
        assertEquals(java.util.List.of("z", "a"), java.util.List.copyOf(e.getData().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> e.getData().put("x", 3));
    }

    /**
     * Tests that creating an event data map with an odd number
     * of key-value arguments throws an {@link IllegalArgumentException}.
     */
    @Test
    void oddNumberOfKeyValuesIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TelemetryEventFactory.data("only-key"));
    }

    /**
     * Tests the typed accessors provided by telemetry events
     * and verifies the detection of background compilation events.
     *
     * <p>Checks numeric and string values, default values for missing keys,
     * and the background status of events based on their metadata.
     */
    @Test
    void typedAccessorsAndBackgroundFlag() {
        TelemetryEventFactory f = new TelemetryEventFactory(new MutableClock());
        TelemetryEvent e = f.of(EventType.COMPILE_ERROR, "line", 12, "message", "oops", "userGenerated", false);
        assertEquals(12, e.getLong("line", -1));
        assertEquals(-1, e.getLong("missing", -1));
        assertEquals("oops", e.getString("message"));
        assertNull(e.getString("missing"));
        assertTrue(e.isBackground());
        assertFalse(f.of(EventType.EDIT).isBackground());
    }

    /**
     * Tests that null event data is treated as an empty map
     * and that telemetry event equality and hash codes are based
     * on event identifiers.
     *
     * <p>Also verifies that the string representation contains
     * the event type.
     */
    @Test
    void nullDataIsTreatedAsEmptyAndEqualityUsesId() {
        TelemetryEvent a = new TelemetryEvent(7, EventType.EDIT, MutableClock.T0, null);
        TelemetryEvent b = new TelemetryEvent(7, EventType.SESSION_ENDED, MutableClock.T0, Map.of());
        assertTrue(a.getData().isEmpty());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("EDIT"));
    }
}