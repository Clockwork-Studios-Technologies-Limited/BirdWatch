package com.team.telemetry.export;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.TelemetryService;
import com.team.telemetry.support.MutableClock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfExporterTest {
    private final PdfExporter exporter = new PdfExporter();

    private static boolean looksLikePdf(byte[] bytes) {
        return bytes.length > 500 && new String(bytes, 0, 5, StandardCharsets.ISO_8859_1).equals("%PDF-");
    }

    @Test
    void rendersPopulatedReportAsPdf() throws IOException {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.write(data, out);

        assertTrue(looksLikePdf(out.toByteArray()));
    }

    @Test
    void rendersEmptySessionWithoutErrors() throws IOException {
        MutableClock clock = new MutableClock();
        TelemetryService service = new TelemetryService(clock, null, Duration.ofSeconds(30));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.write(service.buildReport(), out);

        assertTrue(looksLikePdf(out.toByteArray()));
    }

    @Test
    void truncatesVeryLongTimelinesButStillProducesPdf() throws IOException {
        MutableClock clock = new MutableClock();
        TelemetryService service = new TelemetryService(clock, "s", Duration.ofSeconds(30));
        service.start();
        for (int i = 0; i < PdfExporter.MAX_TIMELINE_ROWS + 20; i++) {
            clock.advanceSeconds(1);
            service.recorder().classStateChanged("C" + i, false, false);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.write(service.buildReport(), out);

        assertTrue(looksLikePdf(out.toByteArray()));
    }

    @Test
    void exportWritesFileAndCreatesParentDirectories(@TempDir Path dir) throws IOException {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        Path target = dir.resolve("reports/out.pdf");

        exporter.export(data, target);

        assertEquals("pdf", exporter.fileExtension());
        assertTrue(Files.size(target) > 500);
    }

    @Test
    void durationFormatterFormatsHoursMinutesSeconds() {
        assertEquals("0:00:00", DurationFormatter.format(0));
        assertEquals("0:00:00", DurationFormatter.format(-5));
        assertEquals("1:01:01", DurationFormatter.format(3_661_000));
    }
}
