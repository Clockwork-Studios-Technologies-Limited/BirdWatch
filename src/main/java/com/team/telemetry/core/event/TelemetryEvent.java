package com.team.telemetry.core.event;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// 3. Jibril contributed this code: added the immutable telemetry event record
/** Immutable, time-stamped telemetry record. */
public final class TelemetryEvent {
    private final long id;
    private final EventType type;
    private final Instant timestamp;
    private final Map<String, Object> data;

    /**
     * Creates an immutable event, copying its data in insertion order.
     *
     * @param id unique event identifier
     * @param type event category; must not be {@code null}
     * @param timestamp event time; must not be {@code null}
     * @param data event attributes, or {@code null} to use an empty map
     * @throws NullPointerException if {@code type} or {@code timestamp} is {@code null}
     */
    public TelemetryEvent(long id, EventType type, Instant timestamp, Map<String, Object> data) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data == null ? Map.of() : data));
    }

    // 4. Jibril contributed this code: added event accessors and typed data lookups
    /**
     * Returns this event's identifier.
     *
     * @return event identifier
     */
    public long getId() { return id; }
    /**
     * Returns this event's category.
     *
     * @return event type
     */
    public EventType getType() { return type; }
    /**
     * Returns the time at which this event was created.
     *
     * @return event timestamp
     */
    public Instant getTimestamp() { return timestamp; }
    /**
     * Returns an unmodifiable, insertion-ordered copy of this event's attributes.
     *
     * @return immutable event data
     */
    public Map<String, Object> getData() { return data; }

    /**
     * Looks up an attribute without converting its value.
     *
     * @param key attribute name
     * @return the attribute value, or {@code null} when the key is absent
     */
    public Object get(String key) { return data.get(key); }

    /**
     * Looks up an attribute and converts a present value to text.
     *
     * @param key attribute name
     * @return the string representation, or {@code null} when the key is absent or maps to null
     */
    public String getString(String key) {
        Object v = data.get(key);
        return v == null ? null : String.valueOf(v);
    }

    /**
     * Looks up an attribute as a number.
     *
     * @param key attribute name
     * @param defaultValue value returned when the key is absent or is not numeric
     * @return the numeric value converted to {@code long}, or {@code defaultValue}
     */
    public long getLong(String key, long defaultValue) {
        Object v = data.get(key);
        return v instanceof Number n ? n.longValue() : defaultValue;
    }

    // 5. Jibril contributed this code: added background event detection
    /**
     * Indicates whether the event is explicitly marked as background-generated.
     *
     * @return {@code true} only when the {@code userGenerated} attribute is {@code false}
     */
    public boolean isBackground() {
        return Boolean.FALSE.equals(data.get("userGenerated"));
    }

    // 6. Jibril contributed this code: added identity-based equality and a readable string form
    /**
     * Compares events by identifier.
     *
     * @param o object to compare
     * @return {@code true} when {@code o} is an event with the same identifier
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof TelemetryEvent other && other.id == id;
    }

    /** @return the hash code derived from this event's identifier */
    @Override
    public int hashCode() { return Long.hashCode(id); }

    /** @return a diagnostic representation containing this event's fields */
    @Override
    public String toString() { return "TelemetryEvent{" + id + ", " + type + ", " + timestamp + ", " + data + "}"; }
}
