package com.team.telemetry.analytics;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Jibril contributed this code: added the activity analyzer with an injectable idle strategy
/**
 * Splits the session into active and idle time. The session is the timeline start -> activity events -> end;
 * each gap between consecutive points is active unless the injected strategy calls it idle.
 */
public class ActivityAnalyzer {
    private final IdleDetectionStrategy idleStrategy;

    /**
     * Creates an analyzer using the supplied idle classification policy.
     *
     * @param idleStrategy policy used to classify gaps between activity points
     */
    public ActivityAnalyzer(IdleDetectionStrategy idleStrategy) {
        this.idleStrategy = idleStrategy;
    }

    // Jibril contributed this code: added session timeline point collection
    /**
     * Calculates active/idle durations and edit totals for a session interval.
     *
     * @param events recorded events to analyze
     * @param sessionStart inclusive start of the analyzed interval
     * @param sessionEnd end of the analyzed interval; values before the start are treated as the start
     * @return activity totals and rates, with background events excluded from activity points
     */
    public ActivitySummary analyze(List<TelemetryEvent> events, Instant sessionStart, Instant sessionEnd) {
        final Instant end = sessionEnd.isBefore(sessionStart) ? sessionStart : sessionEnd;

        List<Instant> points = new ArrayList<>();
        points.add(sessionStart);
        events.stream()
                .filter(e -> e.getType().isActivity() && !e.isBackground())
                .map(TelemetryEvent::getTimestamp)
                .filter(t -> !t.isBefore(sessionStart) && !t.isAfter(end))
                .sorted(Comparator.naturalOrder())
                .forEach(points::add);
        points.add(end);

        // Jibril contributed this code: added active versus idle gap classification
        long active = 0;
        long idle = 0;
        for (int i = 1; i < points.size(); i++) {
            Duration gap = Duration.between(points.get(i - 1), points.get(i));
            if (idleStrategy.isIdle(gap)) {
                idle += gap.toMillis();
            } else {
                active += gap.toMillis();
            }
        }

        // Jibril contributed this code: added edit volume totals
        int edits = 0;
        long added = 0;
        long removed = 0;
        for (TelemetryEvent e : events) {
            if (e.getType() == EventType.EDIT) {
                edits++;
                added += e.getLong("charsAdded", 0);
                removed += e.getLong("charsRemoved", 0);
            }
        }
        // Jibril contributed this code: added typing cadence and edit rate calculation
        double activeMinutes = active / 60_000.0;
        double cadence = activeMinutes > 0 ? added / activeMinutes : 0.0;
        double editRate = activeMinutes > 0 ? edits / activeMinutes : 0.0;
        return new ActivitySummary(active, idle, edits, added, removed, cadence, editRate);
    }
}
