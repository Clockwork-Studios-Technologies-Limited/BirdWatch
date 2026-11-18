package com.team.telemetry.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

// Gabriella contributed this code: added the session metadata value object
/** Session-level facts that head the JSON file and PDF report. Built with the Builder pattern. */
public final class SessionMetadata {
    private final String studentId;
    private final String projectName;
    private final Instant startTime;
    private final Instant endTime;
    private final int totalCompilationAttempts;
    private final int successfulCompilations;
    private final int failedCompilations;
    private final int totalEvents;

    private SessionMetadata(Builder b) {
        this.studentId = b.studentId;
        this.projectName = b.projectName;
        this.startTime = Objects.requireNonNull(b.startTime, "startTime");
        this.endTime = Objects.requireNonNull(b.endTime, "endTime");
        this.totalCompilationAttempts = b.totalCompilationAttempts;
        this.successfulCompilations = b.successfulCompilations;
        this.failedCompilations = b.failedCompilations;
        this.totalEvents = b.totalEvents;
    }

    // Gabriella contributed this code: added session metadata accessors and duration calculation
    /**
     * Returns the student identifier associated with the session.
     *
     * @return student identifier
     */
    public String getStudentId() { return studentId; }
    /**
     * Returns the project name, or an empty string when unavailable.
     *
     * @return project name
     */
    public String getProjectName() { return projectName; }
    /**
     * Returns the session start instant.
     *
     * @return start instant
     */
    public Instant getStartTime() { return startTime; }
    /**
     * Returns the session end instant.
     *
     * @return end instant
     */
    public Instant getEndTime() { return endTime; }
    /**
     * Returns the number of completed user-triggered compilation attempts.
     *
     * @return compilation attempt count
     */
    public int getTotalCompilationAttempts() { return totalCompilationAttempts; }
    /**
     * Returns the number of successful user-triggered compilations.
     *
     * @return successful compilation count
     */
    public int getSuccessfulCompilations() { return successfulCompilations; }
    /**
     * Returns the number of unsuccessful user-triggered compilations.
     *
     * @return failed compilation count
     */
    public int getFailedCompilations() { return failedCompilations; }
    /**
     * Returns the number of events included in the report.
     *
     * @return event count
     */
    public int getTotalEvents() { return totalEvents; }

    /**
     * Returns the non-negative whole-second duration between the session's start and end instants.
     *
     * @return session duration in whole seconds
     */
    public long getDurationSeconds() {
        return Math.max(0, Duration.between(startTime, endTime).getSeconds());
    }

    // Gabriella contributed this code: added the session metadata builder
    /**
     * Creates a builder with default student and project values.
     *
     * @return a new metadata builder
     */
    public static Builder builder() { return new Builder(); }

    /** Fluent builder for immutable {@link SessionMetadata} values. */
    public static final class Builder {
        private String studentId = "STUDENT_ID_PLACEHOLDER";
        private String projectName = "";
        private Instant startTime;
        private Instant endTime;
        private int totalCompilationAttempts;
        private int successfulCompilations;
        private int failedCompilations;
        private int totalEvents;

        /**
         * Sets the student identifier.
         *
         * @param v student identifier to set
         * @return this builder
         */
        public Builder studentId(String v) { this.studentId = v; return this; }
        /**
         * Sets the project name.
         *
         * @param v project name to set
         * @return this builder
         */
        public Builder projectName(String v) { this.projectName = v; return this; }
        /**
         * Sets the required session start instant.
         *
         * @param v session start instant
         * @return this builder
         */
        public Builder startTime(Instant v) { this.startTime = v; return this; }
        /**
         * Sets the required session end instant.
         *
         * @param v session end instant
         * @return this builder
         */
        public Builder endTime(Instant v) { this.endTime = v; return this; }
        /**
         * Sets the number of completed compilation attempts.
         *
         * @param v completed attempt count
         * @return this builder
         */
        public Builder totalCompilationAttempts(int v) { this.totalCompilationAttempts = v; return this; }
        /**
         * Sets the number of successful compilations.
         *
         * @param v successful compilation count
         * @return this builder
         */
        public Builder successfulCompilations(int v) { this.successfulCompilations = v; return this; }
        /**
         * Sets the number of failed compilations.
         *
         * @param v failed compilation count
         * @return this builder
         */
        public Builder failedCompilations(int v) { this.failedCompilations = v; return this; }
        /**
         * Sets the total number of recorded events.
         *
         * @param v event count
         * @return this builder
         */
        public Builder totalEvents(int v) { this.totalEvents = v; return this; }
        /**
         * Creates metadata from the values set on this builder.
         *
         * @return immutable session metadata
         * @throws NullPointerException if a required start or end instant was not set
         */
        public SessionMetadata build() { return new SessionMetadata(this); }
    }
}
