// Jibril contributed this code: Test cases for PdfExporter

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

/**
 * Tests the PDF report generation and file export functionality provided by
 * {@link PdfExporter}.
 *
 * <p>Verifies that populated and empty reports can be rendered, long timelines
 * are handled, exported files are created with the expected extension, and
 * duration values are formatted correctly.
 */
class PdfExporterTest {
    private final PdfExporter exporter = new PdfExporter();

    /**
     * Checks whether the supplied byte array contains a plausible PDF document.
     *
     * <p>The check verifies that the output exceeds a minimum size and begins
     * with the standard PDF file signature.
     *
     * @param bytes the byte array to inspect
     * @return {@code true} if the bytes appear to represent a PDF document;
     *         {@code false} otherwise
     */
    private static boolean looksLikePdf(byte[] bytes) {
        return bytes.length > 500 && new String(bytes, 0, 5, StandardCharsets.ISO_8859_1).equals("%PDF-");
    }

    /**
     * Verifies that a populated telemetry report is rendered as a valid PDF.
     *
     * @throws IOException if writing the PDF report fails
     */
    @Test
    void rendersPopulatedReportAsPdf() throws IOException {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.write(data, out);

        assertTrue(looksLikePdf(out.toByteArray()));
    }

    /**
     * Verifies that an empty telemetry session can be rendered as a PDF
     * without causing errors.
     *
     * @throws IOException if writing the PDF report fails
     */
    @Test
    void rendersEmptySessionWithoutErrors() throws IOException {
        MutableClock clock = new MutableClock();
        TelemetryService service = new TelemetryService(clock, null, Duration.ofSeconds(30));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.write(service.buildReport(), out);

        assertTrue(looksLikePdf(out.toByteArray()));
    }

    /**
     * Verifies that a timeline exceeding the configured row limit is truncated
     * while the exporter still produces a PDF document.
     *
     * @throws IOException if writing the PDF report fails
     */
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

    /**
     * Verifies that exporting a report writes a PDF file and creates any
     * missing parent directories.
     *
     * @param dir the temporary directory used as the export location
     * @throws IOException if the file cannot be created or written
     */
    @Test
    void exportWritesFileAndCreatesParentDirectories(@TempDir Path dir) throws IOException {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        Path target = dir.resolve("reports/out.pdf");

        exporter.export(data, target);

        assertEquals("pdf", exporter.fileExtension());
        assertTrue(Files.size(target) > 500);
    }

    /**
     * Verifies that durations are formatted as hours, minutes, and seconds,
     * including zero and negative input values.
     */
    @Test
    void durationFormatterFormatsHoursMinutesSeconds() {
        assertEquals("0:00:00", DurationFormatter.format(0));
        assertEquals("0:00:00", DurationFormatter.format(-5));
        assertEquals("1:01:01", DurationFormatter.format(3_661_000));
    }
}