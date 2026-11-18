package com.team.telemetry.bluej;

import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;
import com.team.telemetry.TelemetryService;
import java.util.logging.Level;
import java.util.logging.Logger;

// Ihsan contributed this code: added class adapter setup and class state change translation
/** Adapter pattern: BlueJ ClassListener -> TelemetryRecorder calls. */
public class BlueJClassAdapter implements ClassListener {
    private static final Logger LOG = Logger.getLogger(BlueJClassAdapter.class.getName());
    private final TelemetryService service;

    /**
     * Creates a class listener that reports events through the service.
     *
     * @param service telemetry service receiving translated class events
     */
    public BlueJClassAdapter(TelemetryService service) {
        this.service = service;
    }

    /**
     * Reports the class's compiled/error state and inferred first-seen creation.
     *
     * @param event BlueJ class-state event
     */
    @Override
    public void classStateChanged(ClassEvent event) {
        try {
            service.recorder().classStateChanged(event.getBClass().getName(), event.isClassCompiled(), event.hasError());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "classStateChanged failed", e);
        }
    }

    // Ihsan contributed this code: added class rename translation
    /**
     * Clears prior editor tracking and records a class rename.
     *
     * @param event BlueJ class-name event
     */
    @Override
    public void classNameChanged(ClassEvent event) {
        try {
            String oldName = event.getOldName();
            service.editTracker().forget(oldName);
            service.recorder().classRenamed(oldName, event.getBClass().getName());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "classNameChanged failed", e);
        }
    }

    // Ihsan contributed this code: added class removal translation
    /**
     * Clears editor tracking and records class removal.
     *
     * @param event BlueJ class-removal event
     */
    @Override
    public void classRemoved(ClassEvent event) {
        try {
            String name = event.getBClass().getName();
            service.editTracker().forget(name);
            service.recorder().classRemoved(name);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "classRemoved failed", e);
        }
    }
}
