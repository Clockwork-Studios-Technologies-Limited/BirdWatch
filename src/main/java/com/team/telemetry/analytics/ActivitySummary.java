package com.team.telemetry.analytics;

// Jibril contributed this code: added the activity summary record and derived ratios
/**
 * Activity totals and editing rates calculated for a session.
 *
 * @param activeMillis time classified as active, in milliseconds
 * @param idleMillis time classified as idle, in milliseconds
 * @param editCount number of edit events
 * @param charsAdded total inserted characters
 * @param charsRemoved total removed characters
 * @param charsPerActiveMinute inserted characters per active minute
 * @param editsPerActiveMinute edit events per active minute
 */
public record ActivitySummary(long activeMillis, long idleMillis, int editCount, long charsAdded,
                              long charsRemoved, double charsPerActiveMinute, double editsPerActiveMinute) {

    /**
     * Returns the sum of active and idle time.
     *
     * @return total session time in milliseconds
     */
    public long totalMillis() { return activeMillis + idleMillis; }

    /**
     * Calculates the fraction of total time classified as active.
     *
     * @return the active-time fraction, or {@code 0.0} when total time is zero
     */
    public double activeRatio() {
        long total = totalMillis();
        return total == 0 ? 0.0 : (double) activeMillis / total;
    }
}
