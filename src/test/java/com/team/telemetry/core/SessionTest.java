// Jibril contributed this code: Test cases for Session

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.session.SessionManager;
import com.team.telemetry.core.session.SessionMetadata;
import com.team.telemetry.support.MutableClock;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SessionManager} and {@link SessionMetadata}.
 *
 * <p>Verifies session metadata creation, duration calculations,
 * default student identifiers, validation of required timestamps,
 * and session management using a mutable clock.
 */
class SessionTest {

    /**
     * Tests that session metadata correctly calculates the duration
     * between its start and end times.
     *
     * <p>Also verifies that student information, project details,
     * compilation statistics, and total event counts are stored correctly.
     */
    @Test
    void metadataComputesDurationFromStartAndEnd() {
        SessionMetadata m = SessionMetadata.builder()
                .startTime(MutableClock.T0).endTime(MutableClock.T0.plusSeconds(3661))
                .studentId("s1").projectName("p").totalCompilationAttempts(4)
                .successfulCompilations(3).failedCompilations(1).totalEvents(9).build();

        assertEquals(3661, m.getDurationSeconds());
        assertEquals("s1", m.getStudentId());
        assertEquals("p", m.getProjectName());
        assertEquals(4, m.getTotalCompilationAttempts());
        assertEquals(3, m.getSuccessfulCompilations());
        assertEquals(1, m.getFailedCompilations());
        assertEquals(9, m.getTotalEvents());
    }

    /**
     * Tests that negative session durations are clamped to zero
     * and that a placeholder student identifier is assigned
     * when no identifier is provided.
     */
    @Test
    void negativeDurationIsClampedAndPlaceholderIdIsDefault() {
        SessionMetadata m = SessionMetadata.builder()
                .startTime(MutableClock.T0).endTime(MutableClock.T0.minusSeconds(10)).build();
        assertEquals(0, m.getDurationSeconds());
        assertEquals("STUDENT_ID_PLACEHOLDER", m.getStudentId());
    }

    /**
     * Tests that session metadata requires both start and end timestamps.
     *
     * <p>Verifies that building metadata without either required timestamp
     * throws a {@link NullPointerException}.
     */
    @Test
    void metadataRequiresStartAndEnd() {
        assertThrows(NullPointerException.class, () -> SessionMetadata.builder().build());
        assertThrows(NullPointerException.class,
                () -> SessionMetadata.builder().startTime(Instant.EPOCH).build());
    }

    /**
     * Tests that the session manager uses the configured clock
     * to determine session start times.
     *
     * <p>Also verifies placeholder student identifier handling,
     * project name updates, null project name handling, and
     * the creation of session metadata using the current clock time.
     */
    @Test
    void managerUsesClockAndFallsBackToPlaceholderId() {
        MutableClock clock = new MutableClock();
        SessionManager manager = new SessionManager(clock, "  ");
        assertEquals("STUDENT_ID_PLACEHOLDER", manager.getStudentId());
        assertEquals(MutableClock.T0, manager.getStartTime());

        clock.advanceSeconds(10);
        manager.start();
        manager.setProjectName("demo");
        assertEquals(MutableClock.T0.plusSeconds(10), manager.getStartTime());
        assertEquals("demo", manager.getProjectName());
        manager.setProjectName(null);
        assertEquals("", manager.getProjectName());

        SessionMetadata m = manager.newMetadataBuilder().endTime(clock.instant()).build();
        assertEquals(0, m.getDurationSeconds());
    }
}
