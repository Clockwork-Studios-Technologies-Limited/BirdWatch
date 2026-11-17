package com.team.telemetry.core.store;

import com.team.telemetry.core.bus.EventSubscriber;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import java.util.ArrayList;
import java.util.List;

// Jibril contributed this code: added thread-safe in-memory event storage
/** Thread-safe, insertion-ordered store. Also an EventSubscriber so it can listen on the bus. */
public class InMemoryEventRepository implements EventRepository, EventSubscriber {
    private final List<TelemetryEvent> events = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public synchronized void add(TelemetryEvent event) {
        events.add(event);
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(TelemetryEvent event) {
        add(event);
    }

    // Jibril contributed this code: added event queries and clearing
    /** {@inheritDoc} */
    @Override
    public synchronized List<TelemetryEvent> findAll() {
        return List.copyOf(events);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<TelemetryEvent> findByType(EventType type) {
        return events.stream().filter(e -> e.getType() == type).toList();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized int count() {
        return events.size();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void clear() {
        events.clear();
    }
}
