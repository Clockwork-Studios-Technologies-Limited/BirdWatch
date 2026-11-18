package com.team.telemetry.export;

// Ihsan contributed this code: added duration formatting
/** Formats non-negative elapsed millisecond values for report display. */
public final class DurationFormatter {
    private DurationFormatter() { }

    /**
     * Formats milliseconds as {@code H:MM:SS}, clamping negative values to zero.
     *
     * @param millis duration in milliseconds
     * @return formatted duration
     */
    public static String format(long millis) {
        long totalSeconds = Math.max(0, millis) / 1000;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
