package com.team.telemetry;

import com.team.telemetry.analytics.ActivityAnalyzer;
import com.team.telemetry.analytics.CompilationAnalyzer;
import com.team.telemetry.analytics.ThresholdIdleStrategy;
import com.team.telemetry.analytics.TimelineAnalyzer;
import com.team.telemetry.core.bus.EventBus;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.core.parse.CompileErrorClassifier;
import com.team.telemetry.core.parse.SourceScanner;
import com.team.telemetry.core.recorder.EditTracker;
import com.team.telemetry.core.recorder.TelemetryRecorder;
import com.team.telemetry.core.session.SessionManager;
import com.team.telemetry.core.store.InMemoryEventRepository;
import com.team.telemetry.export.JsonExporter;
import com.team.telemetry.export.JsonSerialiser;
import com.team.telemetry.export.PdfExporter;
import com.team.telemetry.export.ReportData;
import com.team.telemetry.export.ReportDataAssembler;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

// Ihsan contributed this code: added the telemetry service facade wiring
/** Facade: wires the whole BlueJ-independent pipeline together using constructor injection. */
public class TelemetryService {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    /**
     * Paths to the JSON and PDF files created by one export operation.
     *
     * @param json path to the exported JSON report
     * @param pdf path to the exported PDF report
     */
    public record ExportResult(Path json, Path pdf) { }

    private final Clock clock;
    private final SessionManager session;
    private final InMemoryEventRepository repository = new InMemoryEventRepository();
    private final TelemetryRecorder recorder;
    private final EditTracker editTracker;
    private final ReportDataAssembler assembler;
    private final JsonExporter jsonExporter = new JsonExporter(new JsonSerialiser());
    private final PdfExporter pdfExporter = new PdfExporter();
    private boolean ended;

    /**
     * Creates and wires the BlueJ-independent telemetry pipeline.
     *
     * @param clock source of event and report timestamps
     * @param studentId student identifier for session metadata
     * @param idleThreshold gap threshold used to classify idle time
     */
    public TelemetryService(Clock clock, String studentId, Duration idleThreshold) {
        this.clock = clock;
        this.session = new SessionManager(clock, studentId);
        EventBus bus = new EventBus();
        bus.subscribe(repository);
        this.recorder = new TelemetryRecorder(new TelemetryEventFactory(clock), bus, new CompileErrorClassifier());
        this.editTracker = new EditTracker(recorder, new SourceScanner());
        this.assembler = new ReportDataAssembler(repository, session,
                new ActivityAnalyzer(new ThresholdIdleStrategy(idleThreshold)),
                new CompilationAnalyzer(), new TimelineAnalyzer(), clock);
    }

    // Ihsan contributed this code: added session start and idempotent end control
    /** Starts a session and records its start event. */
    public void start() {
        session.start();
        recorder.sessionStarted(session.getStudentId());
    }

    /** Ends the session once; repeated calls do not add duplicate end events. */
    public synchronized void end() {
        if (!ended) {
            ended = true;
            recorder.sessionEnded();
        }
    }

    // Ihsan contributed this code: added facade accessors
    /**
     * Returns the recorder used by this service.
     *
     * @return service recorder
     */
    public TelemetryRecorder recorder() { return recorder; }
    /**
     * Returns the editor snapshot tracker used by this service.
     *
     * @return service edit tracker
     */
    public EditTracker editTracker() { return editTracker; }
    /**
     * Sets the project name included in session metadata.
     *
     * @param name project name, or null to clear it
     */
    public void setProjectName(String name) { session.setProjectName(name); }
    /**
     * Builds and returns a report from the current session and recorded events.
     *
     * @return assembled report
     */
    public ReportData buildReport() { return assembler.assemble(); }

    // Ihsan contributed this code: added combined JSON and PDF export
    /**
     * Writes matching JSON and PDF reports into the requested directory.
     *
     * <p>Filenames use the UTC timestamp format {@code telemetry-yyyyMMdd-HHmmss}.</p>
     *
     * @param directory destination directory
     * @return paths to the generated JSON and PDF files
     * @throws IOException if either report cannot be written
     */
    public ExportResult export(Path directory) throws IOException {
        ReportData data = buildReport();
        String base = "telemetry-" + STAMP.format(clock.instant());
        Path json = directory.resolve(base + "." + jsonExporter.fileExtension());
        Path pdf = directory.resolve(base + "." + pdfExporter.fileExtension());
        jsonExporter.export(data, json);
        pdfExporter.export(data, pdf);
        return new ExportResult(json, pdf);
    }
}
