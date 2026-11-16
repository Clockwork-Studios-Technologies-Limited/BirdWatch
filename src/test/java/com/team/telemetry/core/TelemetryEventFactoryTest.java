package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.support.MutableClock;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TelemetryEventFactoryTest {

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

    @Test
    void dataMapKeepsInsertionOrderAndIsImmutable() {
        TelemetryEvent e = new TelemetryEventFactory(new MutableClock()).of(EventType.EDIT, "z", 1, "a", 2);
        assertEquals(java.util.List.of("z", "a"), java.util.List.copyOf(e.getData().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> e.getData().put("x", 3));
    }

    @Test
    void oddNumberOfKeyValuesIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TelemetryEventFactory.data("only-key"));
    }

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
