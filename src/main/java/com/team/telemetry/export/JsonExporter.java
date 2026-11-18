package com.team.telemetry.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Gabriella contributed this code: added JSON file export
/** Writes serialized reports as UTF-8 JSON files. */
public class JsonExporter implements Exporter {
    private final JsonSerialiser serialiser;

    /**
     * Creates a JSON exporter.
     *
     * @param serialiser converts report data to JSON text
     */
    public JsonExporter(JsonSerialiser serialiser) {
        this.serialiser = serialiser;
    }

    /** Returns {@code "json"}. */
    @Override
    public String fileExtension() { return "json"; }

    /**
     * Serializes report data and writes it to the target, creating parent directories as needed.
     *
     * @param data report to export
     * @param target destination file
     * @throws IOException if the destination cannot be written
     */
    @Override
    public void export(ReportData data, Path target) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(target, serialiser.serialise(data), StandardCharsets.UTF_8);
    }
}
