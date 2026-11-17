package com.team.telemetry.analytics;

import com.team.telemetry.core.event.EventType;
import java.time.Instant;

// Ihsan contributed this code: added the timeline entry record
/**
 * A class-creation or method-definition point in the session timeline.
 *
 * @param timestamp time of the event
 * @param offsetSeconds whole seconds from the session start, never negative
 * @param type event category
 * @param className related class name, if present
 * @param methodName related method name, or null for class-creation entries
 */
public record TimelineEntry(Instant timestamp, long offsetSeconds, EventType type, String className,
                            String methodName) {
}
