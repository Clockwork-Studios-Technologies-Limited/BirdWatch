// Jibril contributed this code: Test cases for TelemetryRecorder

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.bus.EventBus;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.core.parse.CompileErrorClassifier;
import com.team.telemetry.core.recorder.TelemetryRecorder;
import com.team.telemetry.core.store.InMemoryEventRepository;
import com.team.telemetry.support.MutableClock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Verifies that values handed over by the BlueJ adapters map onto the right internal events. */
class TelemetryRecorderTest {
    private InMemoryEventRepository repo;
    private TelemetryRecorder recorder;

    @BeforeEach
    void setUp() {
        repo = new InMemoryEventRepository();
        EventBus bus = new EventBus();
        bus.subscribe(repo);
        recorder = new TelemetryRecorder(new TelemetryEventFactory(new MutableClock()), bus, new CompileErrorClassifier());
    }

    private TelemetryEvent last() {
        List<TelemetryEvent> all = repo.findAll();
        return all.get(all.size() - 1);
    }

    @Test
    void compileErrorCarriesFilesLineMessageAndDerivedCode() {
        recorder.compileError(List.of("Foo.java"), 12, "cannot find symbol", true);

        TelemetryEvent e = last();
        assertEquals(EventType.COMPILE_ERROR, e.getType());
        assertEquals(List.of("Foo.java"), e.get("files"));
        assertEquals(12, e.getLong("line", -1));
        assertEquals("cannot find symbol", e.getString("message"));
        assertEquals("compiler.err.cant.resolve", e.getString("code"));
        assertEquals(true, e.get("userGenerated"));
    }

    @Test
    void compileLifecycleEventsMapToTheirTypes() {
        recorder.compileStarted(List.of("A.java"), true);
        recorder.compileWarning(List.of("A.java"), 3, "deprecated", true);
        recorder.compileSucceeded(List.of("A.java"), true);
        recorder.compileFailed(null, false);

        assertEquals(List.of(EventType.COMPILE_STARTED, EventType.COMPILE_WARNING,
                EventType.COMPILE_SUCCEEDED, EventType.COMPILE_FAILED),
                repo.findAll().stream().map(TelemetryEvent::getType).toList());
        assertEquals(List.of(), last().get("files"));
        assertTrue(last().isBackground());
    }

    @Test
    void firstStateChangeOfUnknownClassImpliesCreation() {
        recorder.classStateChanged("Dog", false, false);
        recorder.classStateChanged("Dog", true, false);

        assertEquals(1, repo.findByType(EventType.CLASS_CREATED).size());
        assertEquals(2, repo.findByType(EventType.CLASS_STATE_CHANGED).size());
    }

    @Test
    void classesPresentAtProjectOpenAreNotReportedAsCreated() {
        recorder.registerExistingClasses(List.of("Main"));
        recorder.classStateChanged("Main", true, false);
        assertTrue(repo.findByType(EventType.CLASS_CREATED).isEmpty());
    }

    @Test
    void renameAndRemovalUpdateKnownClasses() {
        recorder.registerExistingClasses(List.of("Old"));
        recorder.classRenamed("Old", "New");
        recorder.classStateChanged("New", true, false);   // known after rename -> no creation
        recorder.classRemoved("New");
        recorder.classStateChanged("New", false, false);  // removed, so it is "created" again

        assertEquals(EventType.CLASS_RENAMED, repo.findAll().get(0).getType());
        assertEquals(1, repo.findByType(EventType.CLASS_REMOVED).size());
        assertEquals(1, repo.findByType(EventType.CLASS_CREATED).size());
    }

    @Test
    void invocationExitTypesMapToExecutionEvents() {
        recorder.invocationFinished("Calc", "calc1", "add", "NORMAL_EXIT", List.of("1", "2"), "3");
        assertEquals(EventType.EXECUTION_FINISHED, last().getType());
        assertEquals("add", last().getString("methodName"));
        assertEquals("3", last().getString("result"));

        recorder.invocationFinished("Calc", "calc1", "div", "EXCEPTION_EXIT", List.of("1", "0"), null);
        assertEquals(EventType.EXECUTION_EXCEPTION, last().getType());
        assertNull(last().get("result"));

        recorder.invocationFinished("Calc", null, "loop", "TERMINATED_EXIT", null, null);
        assertEquals(EventType.EXECUTION_TERMINATED, last().getType());

        recorder.invocationFinished("Calc", "calc2", null, "UNKNOWN_EXIT", List.of(), null);
        assertEquals(EventType.EXECUTION_FINISHED, last().getType());
        assertEquals("<constructor>", last().getString("methodName"));

        recorder.invocationFinished("Calc", "calc3", "x", null, List.of(), null);
        assertEquals(EventType.EXECUTION_FINISHED, last().getType());
    }

    @Test
    void longResultsAreTruncated() {
        recorder.invocationFinished("A", "a", "m", "NORMAL_EXIT", List.of(), "x".repeat(500));
        assertEquals(200 + "...".length(), last().getString("result").length());
    }

    @Test
    void sessionProjectEditAndMethodEvents() {
        recorder.sessionStarted("s123");
        recorder.projectOpened("proj", "");
        recorder.edit("A", 5, 1, 2, 100);
        recorder.methodDefined("A", "foo");
        recorder.projectClosing("proj", "");
        recorder.sessionEnded();

        assertEquals(6, repo.count());
        assertEquals("s123", repo.findAll().get(0).getString("studentId"));
        TelemetryEvent edit = repo.findByType(EventType.EDIT).get(0);
        assertEquals(5, edit.getLong("charsAdded", 0));
        assertEquals(1, edit.getLong("charsRemoved", 0));
        assertEquals(2, edit.getLong("lineDelta", 0));
        assertEquals(100, edit.getLong("totalChars", 0));
    }
}
