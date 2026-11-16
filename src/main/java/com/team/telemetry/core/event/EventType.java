package com.team.telemetry.core.event;

// 1. Ihsan contributed this code: added the telemetry event type catalogue
/** Every kind of telemetry event the plugin records. */
public enum EventType {
    /** A telemetry session started. */
    SESSION_STARTED(false),
    /** A telemetry session ended. */
    SESSION_ENDED(false),
    /** A BlueJ project package was opened. */
    PROJECT_OPENED(true),
    /** A BlueJ project package is closing. */
    PROJECT_CLOSED(false),
    /** A class not already present at project open was first observed. */
    CLASS_CREATED(true),
    /** The compiled or error state of a class changed. */
    CLASS_STATE_CHANGED(false),
    /** A class was renamed. */
    CLASS_RENAMED(true),
    /** A class was removed. */
    CLASS_REMOVED(true),
    /** A previously unseen method or constructor was detected in an editor snapshot. */
    METHOD_DEFINED(false),
    /** A change between editor snapshots was recorded. */
    EDIT(true),
    /** A compilation started. */
    COMPILE_STARTED(true),
    /** A compiler error was reported. */
    COMPILE_ERROR(true),
    /** A compiler warning was reported. */
    COMPILE_WARNING(true),
    /** A compilation completed successfully. */
    COMPILE_SUCCEEDED(true),
    /** A compilation completed unsuccessfully. */
    COMPILE_FAILED(true),
    /** A method or constructor invocation completed normally. */
    EXECUTION_FINISHED(true),
    /** A method or constructor invocation ended with an exception. */
    EXECUTION_EXCEPTION(true),
    /** A method or constructor invocation was terminated. */
    EXECUTION_TERMINATED(true);

    // 2. Ihsan contributed this code: added the activity flag on event types
    private final boolean activity;

    /**
     * Creates an event type with its activity classification.
     *
     * @param activity whether this event type contributes an activity timestamp
     */
    EventType(boolean activity) {
        this.activity = activity;
    }

    /**
     * Indicates whether this event type contributes an activity timestamp.
     *
     * @return {@code true} when events of this type count as activity
     */
    public boolean isActivity() {
        return activity;
    }
}
