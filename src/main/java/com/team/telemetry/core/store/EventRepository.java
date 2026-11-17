package com.team.telemetry.core.store;

import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import java.util.List;

// Jibril contributed this code: added the event repository contract
/** Defines storage and query operations for telemetry events. */
    public interface EventRepository {
    /**
     * Adds an event to the repository.
     *
     * @param event event to store
     */
    void add(TelemetryEvent event);

    /**
     * Returns all stored events.
     *
     * @return events in repository-defined order
     */
    List<TelemetryEvent> findAll();

    /**
     * Finds events with the requested category.
     *
     * @param type event category to match
     * @return matching events in repository-defined order
     */
    List<TelemetryEvent> findByType(EventType type);

    /**
     * Counts stored events.
     *
     * @return number of events
     */
    int count();

    /** Removes all stored events. */
    void clear();
}
