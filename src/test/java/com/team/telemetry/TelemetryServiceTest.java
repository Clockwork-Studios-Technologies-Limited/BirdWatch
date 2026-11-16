// Jibril contributed this code: Test cases for TelemetryService

package com.team.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.support.MutableClock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end through the facade with no BlueJ: record -> aggregate -> export JSON + PDF.
 *
 * <p>Tests the {@link TelemetryService} facade, including timestamped report
 * exports, idempotent session termination, and activity tracking.
 */
class TelemetryServiceTest {

    /**
     * Verifies that exporting a telemetry session creates timestamped JSON
     * and PDF files containing the expected session information.
     *
     * @param dir temporary directory used for the exported files
     * @throws IOException if exporting or reading the generated files fails
     */
    @Test
    void exportWritesTimestampedJsonAndPdf(@TempDir Path dir) throws IOException {
        MutableClock clock = new MutableClock();
        TelemetryService service = new TelemetryService(clock, "s7", Duration.ofSeconds(30));
        service.start();
        service.recorder().compileStarted(java.util.List.of("A.java"), true);
        service.recorder().compileSucceeded(java.util.List.of("A.java"), true);
        clock.advanceSeconds(12);

        TelemetryService.ExportResult result = service.export(dir);

        assertEquals("telemetry-20260101-100012.json", result.json().getFileName().toString());
        assertEquals("telemetry-20260101-100012.pdf", result.pdf().getFileName().toString());
        assertTrue(Files.size(result.pdf()) > 500);
        var root = JsonParser.parseString(Files.readString(result.json())).getAsJsonObject();
        assertEquals("s7", root.getAsJsonObject("session").get("studentId").getAsString());
        assertEquals(1, root.getAsJsonObject("session").get("successfulCompilations").getAsInt());
    }

    /**
     * Verifies that ending a telemetry session multiple times records only
     * one {@link EventType#SESSION_ENDED} event.
     */
    @Test
    void endIsIdempotent() {
        TelemetryService service = new TelemetryService(new MutableClock(), "s", Duration.ofSeconds(30));
        service.start();
        service.end();
        service.end();
        long ended = service.buildReport().getEvents().stream()
                .filter(e -> e.getType() == EventType.SESSION_ENDED).count();
        assertEquals(1, ended);
    }

    /**
     * Verifies that edits made through the service's edit tracker are
     * reflected in the activity metrics of the generated report.
     */
    @Test
    void reportReflectsEditsMadeThroughTheTracker() {
        MutableClock clock = new MutableClock();
        TelemetryService service = new TelemetryService(clock, "s", Duration.ofSeconds(30));
        service.start();
        service.editTracker().onSnapshot("A", "class A {}");
        clock.advanceSeconds(6);
        service.editTracker().onSnapshot("A", "class A { int x; }");

        assertEquals(1, service.buildReport().getActivity().editCount());
        assertEquals(8, service.buildReport().getActivity().charsAdded());
    }
}
