// Jibril contributed this code:

package com.team.telemetry.export;

import com.team.telemetry.TelemetryService;
import com.team.telemetry.support.MutableClock;
import java.time.Duration;
import java.util.List;

/**
 * Provides reusable test data by constructing a representative telemetry session
 * for exporter tests.
 *
 * <p>Creates and populates a {@link TelemetryService} through its public API,
 * simulating a project session that includes project initialization, class
 * creation, source code edits, compilation failures, an idle period, successful
 * compilation, and method invocation.
 *
 * <p>A {@link MutableClock} is used to control the progression of time, ensuring
 * deterministic event timestamps and consistent test results.
 */
final class ExportTestData {

    /**
     * Prevents instantiation of this utility class.
     */
    private ExportTestData() { }

    /**
     * Creates and returns a populated telemetry service containing a realistic
     * sequence of session events.
     *
     * <p>The generated session simulates the following activities:
     * <ol>
     *   <li>Starts a telemetry session for project {@code Zoo}.</li>
     *   <li>Records the opening of the project.</li>
     *   <li>Creates a {@code Dog} class.</li>
     *   <li>Records two source code snapshots representing successive edits.</li>
     *   <li>Starts compilation and records two compilation errors followed by
     *       a failed compilation.</li>
     *   <li>Advances the clock by 60 seconds to simulate user inactivity.</li>
     *   <li>Records a subsequent successful compilation.</li>
     *   <li>Records the successful completion of a {@code bark} method invocation.</li>
     *   <li>Ends the telemetry session.</li>
     * </ol>
     *
     * <p>All events are recorded through the public {@link TelemetryService}
     * API, making the generated data representative of a real application
     * session rather than manually constructed telemetry events.
     *
     * @param clock the mutable clock used to control event timestamps
     * @return a telemetry service containing the completed session and its
     *         recorded events
     */
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
