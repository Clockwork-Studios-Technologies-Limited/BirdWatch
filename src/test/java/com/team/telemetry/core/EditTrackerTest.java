package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.bus.EventBus;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.core.parse.CompileErrorClassifier;
import com.team.telemetry.core.parse.SourceScanner;
import com.team.telemetry.core.recorder.EditTracker;
import com.team.telemetry.core.recorder.TelemetryRecorder;
import com.team.telemetry.core.store.InMemoryEventRepository;
import com.team.telemetry.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditTrackerTest {
    private InMemoryEventRepository repo;
    private EditTracker tracker;

    @BeforeEach
    void setUp() {
        repo = new InMemoryEventRepository();
        EventBus bus = new EventBus();
        bus.subscribe(repo);
        TelemetryRecorder recorder = new TelemetryRecorder(
                new TelemetryEventFactory(new MutableClock()), bus, new CompileErrorClassifier());
        tracker = new EditTracker(recorder, new SourceScanner());
    }

    @Test
    void firstSnapshotIsOnlyABaseline() {
        assertFalse(tracker.onSnapshot("A", "class A {\n}\n"));
        assertEquals(0, repo.count());
    }

    @Test
    void identicalSnapshotRecordsNothing() {
        tracker.onSnapshot("A", "class A {}");
        assertFalse(tracker.onSnapshot("A", "class A {}"));
        assertEquals(0, repo.count());
    }

    @Test
    void insertionProducesEditWithDeltasAndMethodDefinition() {
        String before = "class A {\n}\n";
        String after = "class A {\n void foo() {\n }\n}\n";
        tracker.onSnapshot("A", before);

        assertTrue(tracker.onSnapshot("A", after));

        TelemetryEvent edit = repo.findByType(EventType.EDIT).get(0);
        assertEquals(after.length() - before.length(), edit.getLong("charsAdded", -1));
        assertEquals(0, edit.getLong("charsRemoved", -1));
        assertEquals(2, edit.getLong("lineDelta", -1));
        assertEquals(after.length(), edit.getLong("totalChars", -1));
        assertEquals("foo", repo.findByType(EventType.METHOD_DEFINED).get(0).getString("methodName"));
    }

    @Test
    void deletionCountsRemovedCharacters() {
        tracker.onSnapshot("A", "class A { int abc; }");
        tracker.onSnapshot("A", "class A { int a; }");

        TelemetryEvent edit = repo.findByType(EventType.EDIT).get(0);
        assertEquals(2, edit.getLong("charsRemoved", -1));
        assertEquals(0, edit.getLong("charsAdded", -1));
    }

    @Test
    void replacementCountsBothAddedAndRemoved() {
        tracker.onSnapshot("A", "XaaaY");
        tracker.onSnapshot("A", "XbbY");
        TelemetryEvent edit = repo.findByType(EventType.EDIT).get(0);
        assertEquals(3, edit.getLong("charsRemoved", -1));
        assertEquals(2, edit.getLong("charsAdded", -1));
    }

    @Test
    void existingMethodIsNotReportedTwice() {
        tracker.onSnapshot("A", "class A {\n void foo() {\n }\n}\n");
        tracker.onSnapshot("A", "class A {\n void foo() {\n int x;\n }\n}\n");
        assertTrue(repo.findByType(EventType.METHOD_DEFINED).isEmpty());
    }

    @Test
    void forgetResetsBaselineAndNullsAreIgnored() {
        tracker.onSnapshot("A", "one");
        tracker.forget("A");
        assertFalse(tracker.onSnapshot("A", "two")); // baseline again
        assertFalse(tracker.onSnapshot(null, "x"));
        assertFalse(tracker.onSnapshot("A", null));
        assertEquals(0, repo.count());
    }
}
