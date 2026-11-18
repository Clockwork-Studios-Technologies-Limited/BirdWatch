package com.team.telemetry.export;

import java.io.IOException;
import java.nio.file.Path;

// Jibril contributed this code: added the exporter strategy interface
/** Strategy pattern: interchangeable output formats. */
    public interface Exporter {
    /**
     * Returns the filename extension for this export format.
     *
     * @return extension without a leading period
     */
    String fileExtension();

    /**
     * Writes report data to the target path.
     *
     * @param data report to export
     * @param target destination file
     * @throws IOException if the output cannot be written
     */
    void export(ReportData data, Path target) throws IOException;
}
