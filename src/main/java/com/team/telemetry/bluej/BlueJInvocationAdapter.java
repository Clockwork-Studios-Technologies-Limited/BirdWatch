package com.team.telemetry.bluej;

import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;
import com.team.telemetry.core.recorder.TelemetryRecorder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Jibril contributed this code: added method invocation result translation
/** Adapter pattern: BlueJ InvocationFinishedListener -> TelemetryRecorder calls. */
public class BlueJInvocationAdapter implements InvocationFinishedListener {
    private static final Logger LOG = Logger.getLogger(BlueJInvocationAdapter.class.getName());
    private final TelemetryRecorder recorder;

    /**
     * Creates an invocation listener that forwards results to the recorder.
     *
     * @param recorder destination for translated invocation events
     */
    public BlueJInvocationAdapter(TelemetryRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * Converts a completed BlueJ invocation into a telemetry execution event.
     *
     * @param e completed invocation event
     */
    @Override
    public void invocationFinished(InvocationFinishedEvent e) {
        try {
            String[] params = e.getParameters();
            List<String> parameters = params == null ? Collections.emptyList() : Arrays.asList(params);
            Object result = e.getResult();
            recorder.invocationFinished(e.getClassName(), e.getObjectName(), e.getMethodName(),
                    e.getEventType().name(), parameters, result == null ? null : String.valueOf(result));
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "invocationFinished failed", ex);
        }
    }
}
