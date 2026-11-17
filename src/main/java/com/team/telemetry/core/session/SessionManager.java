package com.team.telemetry.core.session;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

// Ihsan contributed this code: added student session tracking
/** Tracks who the student is, which project is open, and when the session began. */
public class SessionManager {
    private final Clock clock;
    private final String studentId;
    private volatile Instant startTime;
    private volatile String projectName = "";

    /**
     * Creates a session manager and captures an initial start time.
     *
     * @param clock source of session timestamps
     * @param studentId configured student identifier; null or blank values use a placeholder
     * @throws NullPointerException if {@code clock} is {@code null}
     */
    public SessionManager(Clock clock, String studentId) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.studentId = (studentId == null || studentId.isBlank()) ? "STUDENT_ID_PLACEHOLDER" : studentId;
        this.startTime = clock.instant();
    }

    /** Resets the session start time to the current clock instant. */
    public void start() { this.startTime = clock.instant(); }
    /**
     * Returns the current session start time.
     *
     * @return session start instant
     */
    public Instant getStartTime() { return startTime; }
    /**
     * Returns the configured student identifier.
     *
     * @return configured identifier or placeholder
     */
    public String getStudentId() { return studentId; }
    /**
     * Returns the current project name.
     *
     * @return project name, or an empty string when unset
     */
    public String getProjectName() { return projectName; }
    /**
     * Sets the current project name.
     *
     * @param projectName project name, or {@code null} to clear it
     */
    public void setProjectName(String projectName) { this.projectName = projectName == null ? "" : projectName; }

    // Ihsan contributed this code: added a pre-populated session metadata builder
    /**
     * Creates a metadata builder populated from the current session state.
     *
     * @return builder containing student id, project name, and start time
     */
    public SessionMetadata.Builder newMetadataBuilder() {
        return SessionMetadata.builder().studentId(studentId).projectName(projectName).startTime(startTime);
    }
}
