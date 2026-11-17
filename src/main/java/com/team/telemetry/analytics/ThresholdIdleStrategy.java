package com.team.telemetry.analytics;

import java.time.Duration;

// Ihsan contributed this code: added idle threshold validation
/** A gap longer than the threshold is idle. */
public class ThresholdIdleStrategy implements IdleDetectionStrategy {
    private final Duration threshold;

    /**
     * Creates a strategy that considers only gaps longer than {@code threshold} idle.
     *
     * @param threshold positive idle threshold
     * @throws IllegalArgumentException if the threshold is null, zero, or negative
     */
    public ThresholdIdleStrategy(Duration threshold) {
        if (threshold == null || threshold.isNegative() || threshold.isZero()) {
            throw new IllegalArgumentException("Idle threshold must be positive");
        }
        this.threshold = threshold;
    }

    // Ihsan contributed this code: added the threshold idle rule
    /**
     * Tests whether a gap strictly exceeds the configured threshold.
     *
     * @param gap duration to classify
     * @return {@code true} when {@code gap} is longer than the threshold
     */
    @Override
    public boolean isIdle(Duration gap) {
        return gap.compareTo(threshold) > 0;
    }
}
