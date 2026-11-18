package com.team.telemetry.export;

import com.team.telemetry.analytics.ActivitySummary;
import com.team.telemetry.analytics.CompilationSummary;
import com.team.telemetry.analytics.TimelineEntry;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.session.SessionMetadata;
import java.util.List;
import java.util.Objects;

// Ihsan contributed this code: added the report data transfer object
/** Everything the exporters need: a DTO assembled once, consumed by both JSON and PDF. */
public final class ReportData {
    private final SessionMetadata session;
    private final List<TelemetryEvent> events;
    private final ActivitySummary activity;
    private final CompilationSummary compilation;
    private final List<TimelineEntry> timeline;

    private ReportData(Builder b) {
        this.session = Objects.requireNonNull(b.session, "session");
        this.events = List.copyOf(b.events);
        this.activity = Objects.requireNonNull(b.activity, "activity");
        this.compilation = Objects.requireNonNull(b.compilation, "compilation");
        this.timeline = List.copyOf(b.timeline);
    }

    /**
     * Returns session metadata included in the report.
     *
     * @return session metadata
     */
    public SessionMetadata getSession() { return session; }
    /**
     * Returns the immutable list of session events.
     *
     * @return session events
     */
    public List<TelemetryEvent> getEvents() { return events; }
    /**
     * Returns activity totals and rates.
     *
     * @return activity summary
     */
    public ActivitySummary getActivity() { return activity; }
    /**
     * Returns compilation totals and error frequencies.
     *
     * @return compilation summary
     */
    public CompilationSummary getCompilation() { return compilation; }
    /**
     * Returns the immutable class/method timeline.
     *
     * @return timeline entries
     */
    public List<TimelineEntry> getTimeline() { return timeline; }

    // Ihsan contributed this code: added the report data builder
    /**
     * Creates a builder initialized with empty event and timeline lists.
     *
     * @return a new report data builder
     */
    public static Builder builder() { return new Builder(); }

    /** Fluent builder for {@link ReportData}. */
    public static final class Builder {
        private SessionMetadata session;
        private List<TelemetryEvent> events = List.of();
        private ActivitySummary activity;
        private CompilationSummary compilation;
        private List<TimelineEntry> timeline = List.of();

        /**
         * Sets the session metadata.
         *
         * @param v metadata to include
         * @return this builder
         */
        public Builder session(SessionMetadata v) { this.session = v; return this; }
        /**
         * Sets the raw session events.
         *
         * @param v events to include
         * @return this builder
         */
        public Builder events(List<TelemetryEvent> v) { this.events = v; return this; }
        /**
         * Sets the activity summary.
         *
         * @param v summary to include
         * @return this builder
         */
        public Builder activity(ActivitySummary v) { this.activity = v; return this; }
        /**
         * Sets the compilation summary.
         *
         * @param v summary to include
         * @return this builder
         */
        public Builder compilation(CompilationSummary v) { this.compilation = v; return this; }
        /**
         * Sets the class/method timeline.
         *
         * @param v timeline entries to include
         * @return this builder
         */
        public Builder timeline(List<TimelineEntry> v) { this.timeline = v; return this; }
        /**
         * Creates report data from the configured values.
         *
         * @return immutable report data
         * @throws NullPointerException if session metadata or a summary is missing
         */
        public ReportData build() { return new ReportData(this); }
    }
}
