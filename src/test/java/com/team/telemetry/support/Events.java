// Jibril contributed this code:

package com.team.telemetry.support;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/** Builds events at exact timestamps for analyzer tests. */
public final class Events {
    private static final AtomicLong IDS = new AtomicLong();

    private Events() { }

    public static TelemetryEvent at(long secondsAfterStart, EventType type, Object... keyValues) {
        Instant ts = MutableClock.T0.plusSeconds(secondsAfterStart);
        return new TelemetryEvent(IDS.incrementAndGet(), type, ts, TelemetryEventFactory.data(keyValues));
    }
}
