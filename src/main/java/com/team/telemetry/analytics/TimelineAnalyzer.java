package com.team.telemetry.analytics;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Jibril contributed this code: added the class and method progression timeline
/** Builds the progression of class creations and method definitions, ordered by time. */
public class TimelineAnalyzer {

    /**
     * Builds entries for class creations and method definitions.
     *
     * @param events events to scan
     * @param sessionStart origin used to calculate each entry's offset
     * @return matching entries ordered by timestamp
     */
    public List<TimelineEntry> analyze(List<TelemetryEvent> events, Instant sessionStart) {
        List<TimelineEntry> entries = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e.getType() == EventType.CLASS_CREATED) {
                entries.add(entry(e, sessionStart, e.getString("className"), null));
            } else if (e.getType() == EventType.METHOD_DEFINED) {
                entries.add(entry(e, sessionStart, e.getString("className"), e.getString("methodName")));
            }
        }
        entries.sort(Comparator.comparing(TimelineEntry::timestamp));
        return entries;
    }

    // Jibril contributed this code: added timeline offset calculation
    private static TimelineEntry entry(TelemetryEvent e, Instant start, String className, String methodName) {
        long offset = Math.max(0, Duration.between(start, e.getTimestamp()).getSeconds());
        return new TimelineEntry(e.getTimestamp(), offset, e.getType(), className, methodName);
    }
}
