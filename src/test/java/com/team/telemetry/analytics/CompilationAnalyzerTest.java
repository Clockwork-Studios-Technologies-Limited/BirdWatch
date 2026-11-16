// Jibril contributed this code: Test cases for CompilationAnalyzer

package com.team.telemetry.analytics;

import static com.team.telemetry.support.Events.at;
import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompilationAnalyzerTest {
    private final CompilationAnalyzer analyzer = new CompilationAnalyzer();

    @Test
    void countsAttemptsSuccessRateAndSortsErrorFrequency() {
        List<TelemetryEvent> events = List.of(
                at(1, EventType.COMPILE_STARTED, "userGenerated", true),
                at(2, EventType.COMPILE_ERROR, "code", "B", "userGenerated", true),
                at(3, EventType.COMPILE_ERROR, "code", "A", "userGenerated", true),
                at(4, EventType.COMPILE_ERROR, "code", "A", "userGenerated", true),
                at(5, EventType.COMPILE_WARNING, "userGenerated", true),
                at(6, EventType.COMPILE_FAILED, "userGenerated", true),
                at(7, EventType.COMPILE_STARTED, "userGenerated", true),
                at(8, EventType.COMPILE_SUCCEEDED, "userGenerated", true),
                at(9, EventType.COMPILE_SUCCEEDED, "userGenerated", true),
                // BlueJ's background checks must be ignored
                at(10, EventType.COMPILE_ERROR, "code", "Z", "userGenerated", false),
                at(11, EventType.COMPILE_FAILED, "userGenerated", false));

        CompilationSummary s = analyzer.analyze(events);

        assertEquals(3, s.attempts());
        assertEquals(2, s.succeeded());
        assertEquals(1, s.failed());
        assertEquals(1, s.warnings());
        assertEquals(3, s.errorCount());
        assertEquals(2.0 / 3.0, s.successRate(), 1e-9);
        assertEquals(1.0, s.errorsPerAttempt(), 1e-9);
        assertEquals(List.of("A", "B"), List.copyOf(s.errorFrequency().keySet()));
        assertEquals(2, s.errorFrequency().get("A"));
        assertEquals(1, s.errorFrequency().get("B"));
    }

    @Test
    void noCompilationsGivesZeroRatesNotNaN() {
        CompilationSummary s = analyzer.analyze(List.of());
        assertEquals(0, s.attempts());
        assertEquals(0.0, s.successRate());
        assertEquals(0.0, s.errorsPerAttempt());
        assertTrue(s.errorFrequency().isEmpty());
    }

    @Test
    void tiesInFrequencyAreOrderedByCode() {
        CompilationSummary s = analyzer.analyze(List.of(
                at(1, EventType.COMPILE_ERROR, "code", "b"),
                at(2, EventType.COMPILE_ERROR, "code", "a")));
        assertEquals(List.of("a", "b"), List.copyOf(s.errorFrequency().keySet()));
    }

    @Test
    void missingCodeIsCountedAsUnknown() {
        CompilationSummary s = analyzer.analyze(List.of(at(1, EventType.COMPILE_ERROR)));
        assertEquals(1, s.errorFrequency().get("compiler.err.unknown"));
    }

    @Test
    void allFailuresGiveZeroSuccessRate() {
        CompilationSummary s = analyzer.analyze(List.of(
                at(1, EventType.COMPILE_FAILED), at(2, EventType.COMPILE_FAILED)));
        assertEquals(0.0, s.successRate());
        assertEquals(2, s.attempts());
    }
}
