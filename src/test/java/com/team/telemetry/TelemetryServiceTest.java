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

/** End-to-end through the facade with no BlueJ: record -> aggregate -> export JSON + PDF. */
class TelemetryServiceTest {

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
