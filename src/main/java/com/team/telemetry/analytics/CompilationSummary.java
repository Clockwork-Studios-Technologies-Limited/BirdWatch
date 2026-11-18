package com.team.telemetry.analytics;

import java.util.Map;

// Gabriella contributed this code: added the compilation summary record
/**
 * Counts and rates derived from compilation events.
 *
 * @param attempts completed user-triggered compilations, equal to succeeded plus failed
 * @param succeeded successful user-triggered compilations
 * @param failed unsuccessful user-triggered compilations
 * @param warnings warning events from user-triggered compilations
 * @param errorCount individual compile-error events
 * @param errorFrequency counts by derived compiler error code, ordered by descending frequency
 * @param successRate fraction of completed attempts that succeeded, or zero when there are no attempts
 * @param errorsPerAttempt compile errors divided by completed attempts, or zero when there are no attempts
 */
public record CompilationSummary(int attempts, int succeeded, int failed, int warnings, int errorCount,
                                 Map<String, Integer> errorFrequency, double successRate,
                                 double errorsPerAttempt) {
}
