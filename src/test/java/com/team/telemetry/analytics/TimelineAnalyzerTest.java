// Jibril contributed this code: Test cases for TimelineAnalyzer

package com.team.telemetry.analytics;

import static com.team.telemetry.support.Events.at;
import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.support.MutableClock;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimelineAnalyzerTest {

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

    @Test
    void eventsBeforeSessionStartHaveZeroOffset() {
        List<TimelineEntry> t = new TimelineAnalyzer().analyze(
                List.of(at(-10, EventType.CLASS_CREATED, "className", "X")), MutableClock.T0);
        assertEquals(0, t.get(0).offsetSeconds());
    }

    @Test
    void noRelevantEventsGivesEmptyTimeline() {
        assertTrue(new TimelineAnalyzer().analyze(List.of(at(1, EventType.EDIT)), MutableClock.T0).isEmpty());
    }
}
