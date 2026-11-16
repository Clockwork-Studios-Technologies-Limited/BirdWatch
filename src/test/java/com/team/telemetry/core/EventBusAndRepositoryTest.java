// Jibril contributed this code: Test cases for EventBusAndRepository

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.bus.EventBus;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.core.store.InMemoryEventRepository;
import com.team.telemetry.support.MutableClock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventBusAndRepositoryTest {
    private final TelemetryEventFactory factory = new TelemetryEventFactory(new MutableClock());

    @Test
    void publishDeliversToAllSubscribers() {
        EventBus bus = new EventBus();
        List<TelemetryEvent> a = new ArrayList<>();
        List<TelemetryEvent> b = new ArrayList<>();
        bus.subscribe(a::add);
        bus.subscribe(b::add);

        bus.publish(factory.of(EventType.EDIT));

        assertEquals(1, a.size());
        assertEquals(1, b.size());
        assertEquals(2, bus.subscriberCount());
    }

    @Test
    void failingSubscriberDoesNotBlockOthers() {
        EventBus bus = new EventBus();
        List<TelemetryEvent> received = new ArrayList<>();
        bus.subscribe(e -> { throw new IllegalStateException("boom"); });
        bus.subscribe(received::add);

        bus.publish(factory.of(EventType.EDIT));

        assertEquals(1, received.size());
    }

    @Test
    void unsubscribedListenerReceivesNothing() {
        EventBus bus = new EventBus();
        List<TelemetryEvent> received = new ArrayList<>();
        com.team.telemetry.core.bus.EventSubscriber sub = received::add;
        bus.subscribe(sub);
        bus.unsubscribe(sub);
        bus.publish(factory.of(EventType.EDIT));
        assertTrue(received.isEmpty());
    }

    @Test
    void nullSubscriberRejected() {
        assertThrows(NullPointerException.class, () -> new EventBus().subscribe(null));
    }

    @Test
    void repositoryStoresInOrderFiltersByTypeAndClears() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        EventBus bus = new EventBus();
        bus.subscribe(repo);

        bus.publish(factory.of(EventType.EDIT));
        bus.publish(factory.of(EventType.COMPILE_FAILED));
        bus.publish(factory.of(EventType.EDIT));

        assertEquals(3, repo.count());
        assertEquals(2, repo.findByType(EventType.EDIT).size());
        assertEquals(EventType.COMPILE_FAILED, repo.findAll().get(1).getType());
        repo.clear();
        assertEquals(0, repo.count());
    }
}
