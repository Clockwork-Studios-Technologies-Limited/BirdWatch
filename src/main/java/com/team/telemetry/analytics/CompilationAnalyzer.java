package com.team.telemetry.analytics;

import com.team.telemetry.core.event.TelemetryEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Gabriella contributed this code: added compile outcome counting
/** Aggregates compile outcomes and error frequencies. BlueJ's own background checks are ignored. */
public class CompilationAnalyzer {

    /**
     * Counts user-generated compilation outcomes and groups errors by derived code.
     *
     * @param events events to analyze; background events are ignored
     * @return compilation counts, error frequencies, and rates
     */
    public CompilationSummary analyze(List<TelemetryEvent> events) {
        int succeeded = 0;
        int failed = 0;
        int warnings = 0;
        int errors = 0;
        Map<String, Integer> counts = new HashMap<>();

        for (TelemetryEvent e : events) {
            if (e.isBackground()) {
                continue;
            }
            switch (e.getType()) {
                case COMPILE_SUCCEEDED -> succeeded++;
                case COMPILE_FAILED -> failed++;
                case COMPILE_WARNING -> warnings++;
                case COMPILE_ERROR -> {
                    errors++;
                    String code = e.getString("code");
                    counts.merge(code == null ? "compiler.err.unknown" : code, 1, Integer::sum);
                }
                default -> { }
            }
        }

        // Gabriella contributed this code: added error frequency ranking
        Map<String, Integer> sorted = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(en -> sorted.put(en.getKey(), en.getValue()));

        // Gabriella contributed this code: added compilation success metrics
        int attempts = succeeded + failed;
        double rate = attempts == 0 ? 0.0 : (double) succeeded / attempts;
        double errorsPerAttempt = attempts == 0 ? 0.0 : (double) errors / attempts;
        return new CompilationSummary(attempts, succeeded, failed, warnings, errors, sorted, rate, errorsPerAttempt);
    }
}
