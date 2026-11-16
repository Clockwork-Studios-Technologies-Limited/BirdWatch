// Jibril contributed this code: Test cases for TimelineAnalyzer

package com.team.telemetry.analytics;

import static com.team.telemetry.support.Events.at;
import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.support.MutableClock;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TimelineAnalyzer}.
 *
 * <p>Verifies that relevant telemetry events are converted into timeline
 * entries, ordered chronologically, and assigned the correct offsets
 * relative to the session start time.
 */
class TimelineAnalyzerTest {

    /**
     * Tests that class creation and method definition events are listed
     * in chronological order.
     *
     * <p>Verifies that each timeline entry contains the correct event type,
     * class name, method name, and offset from the session start.
     * Irrelevant events are excluded from the timeline.
     */
    @Test
    void listsClassCreationsAndMethodDefinitionsInTimeOrder() {
        List<TelemetryEvent> events = List.of(
                at(40, EventType.METHOD_DEFINED, "className", "Dog", "methodName", "bark"),
                at(5, EventType.CLASS_CREATED, "className", "Dog"),
                at(20, EventType.EDIT),
                at(30, EventType.CLASS_CREATED, "className", "Cat"));

        List<TimelineEntry> timeline = new TimelineAnalyzer().analyze(events, MutableClock.T0);

        assertEquals(3, timeline.size());
        assertEquals(EventType.CLASS_CREATED, timeline.get(0).type());
        assertEquals("Dog", timeline.get(0).className());
        assertNull(timeline.get(0).methodName());
        assertEquals(5, timeline.get(0).offsetSeconds());
        assertEquals("Cat", timeline.get(1).className());
        assertEquals("bark", timeline.get(2).methodName());
        assertEquals(40, timeline.get(2).offsetSeconds());
    }

    /**
     * Tests that events occurring before the session start time
     * are assigned an offset of zero seconds.
     */
    @Test
    void eventsBeforeSessionStartHaveZeroOffset() {
        List<TimelineEntry> t = new TimelineAnalyzer().analyze(
                List.of(at(-10, EventType.CLASS_CREATED, "className", "X")), MutableClock.T0);
        assertEquals(0, t.get(0).offsetSeconds());
    }

    /**
     * Tests that an event list containing no relevant timeline events
     * produces an empty timeline.
     */
    @Test
    void noRelevantEventsGivesEmptyTimeline() {
        assertTrue(new TimelineAnalyzer().analyze(List.of(at(1, EventType.EDIT)), MutableClock.T0).isEmpty());
    }
}
