package com.team.telemetry.export;

import com.team.telemetry.analytics.ActivityAnalyzer;
import com.team.telemetry.analytics.CompilationAnalyzer;
import com.team.telemetry.analytics.CompilationSummary;
import com.team.telemetry.analytics.TimelineAnalyzer;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.session.SessionManager;
import com.team.telemetry.core.session.SessionMetadata;
import com.team.telemetry.core.store.EventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

// Jibril contributed this code: added the report assembler wiring
/** Runs all analyzers over the repository and packages the result for the exporters. */
public class ReportDataAssembler {
    private final EventRepository repository;
    private final SessionManager session;
    private final ActivityAnalyzer activityAnalyzer;
    private final CompilationAnalyzer compilationAnalyzer;
    private final TimelineAnalyzer timelineAnalyzer;
    private final Clock clock;

    /**
     * Creates an assembler using the repository, session state, analyzers, and clock.
     *
     * @param repository source of recorded events
     * @param session source of session identity and start time
     * @param activityAnalyzer calculates activity totals
     * @param compilationAnalyzer calculates compilation totals
     * @param timelineAnalyzer creates progression entries
     * @param clock supplies the report end time
     */
    public ReportDataAssembler(EventRepository repository, SessionManager session, ActivityAnalyzer activityAnalyzer,
                               CompilationAnalyzer compilationAnalyzer, TimelineAnalyzer timelineAnalyzer,
                               Clock clock) {
        this.repository = repository;
        this.session = session;
        this.activityAnalyzer = activityAnalyzer;
        this.compilationAnalyzer = compilationAnalyzer;
        this.timelineAnalyzer = timelineAnalyzer;
        this.clock = clock;
    }

    // Jibril contributed this code: added session metadata assembly
    /**
     * Builds report data from the current session state and all stored events.
     *
     * @return assembled immutable report data
     */
    public ReportData assemble() {
        Instant end = clock.instant();
        Instant start = session.getStartTime();
        List<TelemetryEvent> events = repository.findAll();
        CompilationSummary compilation = compilationAnalyzer.analyze(events);

        SessionMetadata metadata = session.newMetadataBuilder()
                .endTime(end)
                .totalCompilationAttempts(compilation.attempts())
                .successfulCompilations(compilation.succeeded())
                .failedCompilations(compilation.failed())
                .totalEvents(events.size())
                .build();

        // Jibril contributed this code: added final report assembly
        return ReportData.builder()
                .session(metadata)
                .events(events)
                .activity(activityAnalyzer.analyze(events, start, end))
                .compilation(compilation)
                .timeline(timelineAnalyzer.analyze(events, start))
                .build();
    }
}
