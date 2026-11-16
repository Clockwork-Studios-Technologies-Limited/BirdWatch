// Jibril contributed this code: Test cases for JsonSerialiser

package com.team.telemetry.export;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.team.telemetry.support.MutableClock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link JsonSerialiser}.
 *
 * <p>Verifies that report data is correctly converted into JSON,
 * follows the documented export schema, contains valid timestamps
 * and sequential event identifiers, and can be exported to a file.
 */
class JsonSerialiserTest {
    private final JsonSerialiser serialiser = new JsonSerialiser();

    /**
     * Tests that the serialiser produces valid JSON containing
     * all required fields in the documented report schema.
     *
     * <p>Verifies session metadata, activity statistics, compilation
     * summaries, error frequencies, telemetry events, and timeline entries.
     */
    @Test
    void producesValidJsonWithTheDocumentedSchema() {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();

        JsonObject root = JsonParser.parseString(serialiser.serialise(data)).getAsJsonObject();

        assertEquals(JsonSerialiser.SCHEMA_VERSION, root.get("schemaVersion").getAsInt());

        JsonObject session = root.getAsJsonObject("session");
        assertEquals("s42", session.get("studentId").getAsString());
        assertEquals("Zoo", session.get("projectName").getAsString());
        assertEquals("2026-01-01T10:00:00Z", session.get("startTime").getAsString());
        assertEquals(90, session.get("durationSeconds").getAsLong());
        assertEquals(2, session.get("totalCompilationAttempts").getAsInt());
        assertEquals(data.getEvents().size(), session.get("totalEvents").getAsInt());

        JsonObject activity = root.getAsJsonObject("summary").getAsJsonObject("activity");
        assertEquals(30, activity.get("activeSeconds").getAsLong());
        assertEquals(60, activity.get("idleSeconds").getAsLong());
        assertEquals(0.33, activity.get("activeRatio").getAsDouble(), 1e-9);

        JsonObject compilation = root.getAsJsonObject("summary").getAsJsonObject("compilation");
        assertEquals(0.5, compilation.get("successRate").getAsDouble(), 1e-9);
        assertEquals(1, compilation.getAsJsonObject("errorFrequency")
                .get("compiler.err.cant.resolve").getAsInt());

        JsonArray events = root.getAsJsonArray("events");
        assertEquals(data.getEvents().size(), events.size());
        JsonObject first = events.get(0).getAsJsonObject();
        assertEquals(1, first.get("id").getAsInt());
        assertEquals("SESSION_STARTED", first.get("type").getAsString());
        assertNotNull(first.get("timestamp"));
        assertEquals("s42", first.getAsJsonObject("data").get("studentId").getAsString());

        JsonArray timeline = root.getAsJsonArray("timeline");
        assertEquals("CLASS_CREATED", timeline.get(0).getAsJsonObject().get("type").getAsString());
    }

    /**
     * Tests that every serialized telemetry event contains a valid
     * ISO-8601 timestamp and that event identifiers are strictly increasing.
     */
    @Test
    void everyEventHasAnIsoTimestampAndIdsAreIncreasing() {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        JsonArray events = serialiser.toJson(data).getAsJsonArray("events");
        long previous = 0;
        for (var el : events) {
            JsonObject e = el.getAsJsonObject();
            assertDoesNotThrow(() -> java.time.Instant.parse(e.get("timestamp").getAsString()));
            assertTrue(e.get("id").getAsLong() > previous);
            previous = e.get("id").getAsLong();
        }
    }

    /**
     * Tests that the JSON exporter writes a valid, parseable JSON file
     * to the specified destination.
     *
     * <p>Also verifies that missing parent directories are created,
     * the exporter reports the correct file extension, and the
     * exported file uses UTF-8 encoding.
     *
     * @param dir temporary directory used for the export test
     * @throws IOException if an error occurs while writing or reading the file
     */
    @Test
    void exporterWritesParseableFileAndCreatesDirectories(@TempDir Path dir) throws IOException {
        ReportData data = ExportTestData.populatedService(new MutableClock()).buildReport();
        Path target = dir.resolve("nested/out.json");

        JsonExporter exporter = new JsonExporter(serialiser);
        exporter.export(data, target);

        assertEquals("json", exporter.fileExtension());
        String text = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(JsonParser.parseString(text).isJsonObject());
    }
}