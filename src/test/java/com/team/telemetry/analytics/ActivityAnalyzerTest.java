// Jibril contributed this code: Test cases for ActivityAnalyzer

package com.team.telemetry.analytics;

import static com.team.telemetry.support.Events.at;
import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ActivityAnalyzer}.
 *
 * <p>Verifies the calculation of active and idle time within a session, including
 * activity ratios, editing statistics, and productivity metrics. Also tests
 * boundary conditions, event filtering, chronological ordering, and validation
 * of idle detection thresholds.
 *
 * <p>The analyzer uses a {@link ThresholdIdleStrategy} with a 30-second threshold
 * to determine whether gaps between activity events are considered active or idle.
 */
class ActivityAnalyzerTest {
    private static final Instant START = MutableClock.T0;
    private final ActivityAnalyzer analyzer = new ActivityAnalyzer(new ThresholdIdleStrategy(Duration.ofSeconds(30)));

    /**
     * Verifies that active and idle durations are calculated correctly from
     * the gaps between activity events.
     *
     * <p>Also verifies that the analyzer correctly calculates the total session
     * duration, active ratio, edit count, character additions and removals,
     * and editing productivity rates.
     */
    @Test
    void sumsActiveAndIdleGapsExactly() {
        List<TelemetryEvent> events = List.of(
                at(10, EventType.EDIT, "charsAdded", 20, "charsRemoved", 0),
                at(20, EventType.EDIT, "charsAdded", 10, "charsRemoved", 4),
                at(100, EventType.EDIT, "charsAdded", 0, "charsRemoved", 1));

        ActivitySummary s = analyzer.analyze(events, START, START.plusSeconds(110));

        // gaps: 10 (active), 10 (active), 80 (idle), 10 (active)
        assertEquals(30_000, s.activeMillis());
        assertEquals(80_000, s.idleMillis());
        assertEquals(110_000, s.totalMillis());
        assertEquals(30.0 / 110.0, s.activeRatio(), 1e-9);
        assertEquals(3, s.editCount());
        assertEquals(30, s.charsAdded());
        assertEquals(5, s.charsRemoved());
        assertEquals(30 / 0.5, s.charsPerActiveMinute(), 1e-9);
        assertEquals(3 / 0.5, s.editsPerActiveMinute(), 1e-9);
    }

    /**
     * Verifies that a gap equal to the idle threshold is classified as active
     * rather than idle.
     */
    @Test
    void gapExactlyAtThresholdIsStillActive() {
        ActivitySummary s = analyzer.analyze(List.of(at(30, EventType.EDIT)), START, START.plusSeconds(60));
        assertEquals(60_000, s.activeMillis());
        assertEquals(0, s.idleMillis());
    }

    /**
     * Verifies that a session containing no events is classified entirely as
     * idle when its duration exceeds the configured idle threshold.
     *
     * <p>Also ensures that activity-dependent metrics return zero when no
     * active time is recorded.
     */
    @Test
    void emptySessionIsEntirelyIdleWhenLongerThanThreshold() {
        ActivitySummary s = analyzer.analyze(List.of(), START, START.plusSeconds(120));
        assertEquals(0, s.activeMillis());
        assertEquals(120_000, s.idleMillis());
        assertEquals(0.0, s.activeRatio());
        assertEquals(0.0, s.charsPerActiveMinute());
        assertEquals(0.0, s.editsPerActiveMinute());
    }

    /**
     * Verifies that a zero-length session produces zero total duration and
     * a zero active ratio without causing division-by-zero errors.
     */
    @Test
    void zeroLengthSessionHasZeroRatioWithoutDividingByZero() {
        ActivitySummary s = analyzer.analyze(List.of(), START, START);
        assertEquals(0, s.totalMillis());
        assertEquals(0.0, s.activeRatio());
    }


    /**
     * Verifies that a session whose end time precedes its start time is
     * treated as a zero-length session.
     */
    @Test
    void endBeforeStartIsClampedToZeroLength() {
        ActivitySummary s = analyzer.analyze(List.of(), START, START.minusSeconds(5));
        assertEquals(0, s.totalMillis());
    }

    /**
     * Verifies that background events and events unrelated to user activity
     * do not count as activity or interrupt idle periods.
     */
    @Test
    void backgroundAndNonActivityEventsDoNotBreakIdleGaps() {
        List<TelemetryEvent> events = List.of(
                at(50, EventType.COMPILE_FAILED, "userGenerated", false),   // BlueJ background check
                at(55, EventType.CLASS_STATE_CHANGED),                      // not an activity event
                at(60, EventType.METHOD_DEFINED));                          // not an activity event

        ActivitySummary s = analyzer.analyze(events, START, START.plusSeconds(60));
        assertEquals(0, s.activeMillis());
        assertEquals(60_000, s.idleMillis());
    }

    /**
     * Verifies that events occurring outside the session time window are
     * ignored and that the analyzer produces correct results regardless
     * of the order in which events are supplied.
     */
    @Test
    void eventsOutsideTheSessionWindowAreIgnoredAndOrderDoesNotMatter() {
        List<TelemetryEvent> events = List.of(
                at(20, EventType.EDIT), at(10, EventType.EDIT), at(500, EventType.EDIT), at(-5, EventType.EDIT));
        ActivitySummary s = analyzer.analyze(events, START, START.plusSeconds(30));
        assertEquals(30_000, s.activeMillis());
        assertEquals(0, s.idleMillis());
    }

    /**
     * Verifies that {@link ThresholdIdleStrategy} rejects invalid threshold
     * values, including zero, negative durations, and null.
     */
    @Test
    void thresholdMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new ThresholdIdleStrategy(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdIdleStrategy(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdIdleStrategy(null));
    }
}
