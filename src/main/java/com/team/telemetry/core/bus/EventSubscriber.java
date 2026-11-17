package com.team.telemetry.core.bus;

import com.team.telemetry.core.event.TelemetryEvent;

// Ihsan contributed this code: added the event subscriber contract
/** Observer side of the event bus. */
@FunctionalInterface
    public interface EventSubscriber {
    /**
     * Receives a published telemetry event.
     *
     * @param event event delivered by the bus
     */
    void onEvent(TelemetryEvent event);
}
