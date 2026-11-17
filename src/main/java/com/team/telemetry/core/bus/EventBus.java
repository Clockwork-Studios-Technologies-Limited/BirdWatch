package com.team.telemetry.core.bus;

import com.team.telemetry.core.event.TelemetryEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Ihsan contributed this code: added subscriber registration on the event bus
/** Observer pattern: decouples event producers (BlueJ adapters) from consumers (repository, etc.). */
public class EventBus {
    private static final Logger LOG = Logger.getLogger(EventBus.class.getName());
    private final List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();

    /**
     * Registers a subscriber for subsequent publications.
     *
     * @param subscriber subscriber to add
     * @throws NullPointerException if {@code subscriber} is {@code null}
     */
    public void subscribe(EventSubscriber subscriber) {
        subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
    }

    /**
     * Removes a subscriber if it is currently registered.
     *
     * @param subscriber subscriber to remove
     */
    public void unsubscribe(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Returns the number of registered subscribers.
     *
     * @return current subscriber count
     */
    public int subscriberCount() {
        return subscribers.size();
    }

    // Ihsan contributed this code: added fault-isolated event publishing
    /**
     * Delivers an event to each subscriber, logging and isolating subscriber runtime failures.
     *
     * @param event event to publish
     */
    public void publish(TelemetryEvent event) {
        for (EventSubscriber s : subscribers) {
            try {
                s.onEvent(event);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Subscriber failed for " + event.getType(), e);
            }
        }
    }
}
