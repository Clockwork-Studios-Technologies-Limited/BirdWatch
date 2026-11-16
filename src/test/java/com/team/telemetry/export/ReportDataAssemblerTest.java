// Jibril contributed this code: Test cases for ReportDataAssembler


package com.team.telemetry.export;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.TelemetryService;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.support.MutableClock;
import org.junit.jupiter.api.Test;

class ReportDataAssemblerTest {

    @Test
    void assemblesSessionSummariesTimelineAndEvents() {
        MutableClock clock = new MutableClock();
        TelemetryService service = ExportTestData.populatedService(clock);

        ReportData data = service.buildReport();

        assertEquals("s42", data.getSession().getStudentId());
        assertEquals("Zoo", data.getSession().getProjectName());
        assertEquals(90, data.getSession().getDurationSeconds());
        assertEquals(2, data.getSession().getTotalCompilationAttempts());
        assertEquals(1, data.getSession().getSuccessfulCompilations());
        assertEquals(1, data.getSession().getFailedCompilations());
        assertEquals(data.getEvents().size(), data.getSession().getTotalEvents());

        // 60s gap with nothing in it is the only idle stretch
        assertEquals(60_000, data.getActivity().idleMillis());
        assertEquals(30_000, data.getActivity().activeMillis());
        assertEquals(1, data.getActivity().editCount());

        assertEquals(2, data.getCompilation().errorCount());
        assertEquals(2, data.getCompilation().errorFrequency().size());

        assertEquals(EventType.CLASS_CREATED, data.getTimeline().get(0).type());
        assertEquals("bark", data.getTimeline().get(1).methodName());

        assertEquals(EventType.SESSION_STARTED, data.getEvents().get(0).getType());
        assertEquals(EventType.SESSION_ENDED, data.getEvents().get(data.getEvents().size() - 1).getType());
    }

    @Test
    void reportDataBuilderRequiresSessionAndSummaries() {
        assertThrows(NullPointerException.class, () -> ReportData.builder().build());
    }
}
