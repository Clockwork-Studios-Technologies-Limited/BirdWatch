package com.team.telemetry.export;

import com.team.telemetry.TelemetryService;
import com.team.telemetry.support.MutableClock;
import java.time.Duration;
import java.util.List;

/** Builds a realistic session through the public pipeline for exporter tests. */
final class ExportTestData {
    private ExportTestData() { }

    static TelemetryService populatedService(MutableClock clock) {
        TelemetryService service = new TelemetryService(clock, "s42", Duration.ofSeconds(30));
        service.start();
        service.setProjectName("Zoo");
        service.recorder().projectOpened("Zoo", "");
        clock.advanceSeconds(5);
        service.recorder().classStateChanged("Dog", false, false);        // CLASS_CREATED
        service.editTracker().onSnapshot("Dog", "class Dog {\n}\n");
        clock.advanceSeconds(10);
        service.editTracker().onSnapshot("Dog", "class Dog {\n void bark() {\n }\n}\n");
        clock.advanceSeconds(5);
        service.recorder().compileStarted(List.of("Dog.java"), true);
        service.recorder().compileError(List.of("Dog.java"), 2, "cannot find symbol", true);
        service.recorder().compileError(List.of("Dog.java"), 3, "';' expected", true);
        service.recorder().compileFailed(List.of("Dog.java"), true);
        clock.advanceSeconds(60);                                          // idle stretch
        service.recorder().compileStarted(List.of("Dog.java"), true);
        service.recorder().compileSucceeded(List.of("Dog.java"), true);
        clock.advanceSeconds(5);
        service.recorder().invocationFinished("Dog", "dog1", "bark", "NORMAL_EXIT", List.of(), null);
        clock.advanceSeconds(5);
        service.end();
        return service;
    }
}
