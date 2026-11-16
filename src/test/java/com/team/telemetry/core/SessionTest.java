package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.session.SessionManager;
import com.team.telemetry.core.session.SessionMetadata;
import com.team.telemetry.support.MutableClock;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionTest {

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

    @Test
    void negativeDurationIsClampedAndPlaceholderIdIsDefault() {
        SessionMetadata m = SessionMetadata.builder()
                .startTime(MutableClock.T0).endTime(MutableClock.T0.minusSeconds(10)).build();
        assertEquals(0, m.getDurationSeconds());
        assertEquals("STUDENT_ID_PLACEHOLDER", m.getStudentId());
    }

    @Test
    void metadataRequiresStartAndEnd() {
        assertThrows(NullPointerException.class, () -> SessionMetadata.builder().build());
        assertThrows(NullPointerException.class,
                () -> SessionMetadata.builder().startTime(Instant.EPOCH).build());
    }

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
