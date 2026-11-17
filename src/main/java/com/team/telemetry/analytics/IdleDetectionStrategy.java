package com.team.telemetry.analytics;

import java.time.Duration;

// Ihsan contributed this code: added the idle detection strategy contract
/** Strategy pattern: decides whether a gap between two activity events counts as idle time. */
@FunctionalInterface
    public interface IdleDetectionStrategy {
    /**
     * Classifies the time gap between activity points.
     *
     * @param gap duration to classify
     * @return {@code true} when the gap should count as idle
     */
    boolean isIdle(Duration gap);
}
