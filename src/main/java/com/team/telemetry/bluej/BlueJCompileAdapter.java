package com.team.telemetry.bluej;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;
import com.team.telemetry.core.recorder.TelemetryRecorder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Gabriella contributed this code: added compile adapter setup and file name extraction
/** Adapter pattern: BlueJ CompileListener -> TelemetryRecorder calls. */
public class BlueJCompileAdapter implements CompileListener {
    private final TelemetryRecorder recorder;

    /**
     * Creates a compile listener that forwards events to the recorder.
     *
     * @param recorder destination for translated compile events
     */
    public BlueJCompileAdapter(TelemetryRecorder recorder) {
        this.recorder = recorder;
    }

    private static List<String> names(CompileEvent e) {
        List<String> names = new ArrayList<>();
        File[] files = e.getFiles();
        if (files != null) {
            for (File f : files) {
                names.add(f.getName());
            }
        }
        return names;
    }

    // Gabriella contributed this code: added compile event translation
    /**
     * Forwards a BlueJ compilation-start callback to the recorder.
     *
     * @param e BlueJ compile-start event
     */
    @Override
    public void compileStarted(CompileEvent e) {
        recorder.compileStarted(names(e), e.isUserGeneratedCompilation());
    }

    /**
     * Forwards a BlueJ compiler-error callback, including its message and source line.
     *
     * @param e BlueJ compile-error event
     */
    @Override
    public void compileError(CompileEvent e) {
        recorder.compileError(names(e), e.getErrorLineNumber(), e.getErrorMessage(), e.isUserGeneratedCompilation());
    }

    /**
     * Forwards a BlueJ compiler-warning callback, including its message and source line.
     *
     * @param e BlueJ compile-warning event
     */
    @Override
    public void compileWarning(CompileEvent e) {
        recorder.compileWarning(names(e), e.getErrorLineNumber(), e.getErrorMessage(), e.isUserGeneratedCompilation());
    }

    /**
     * Forwards a successful BlueJ compilation callback.
     *
     * @param e BlueJ successful-compilation event
     */
    @Override
    public void compileSucceeded(CompileEvent e) {
        recorder.compileSucceeded(names(e), e.isUserGeneratedCompilation());
    }

    /**
     * Forwards a failed BlueJ compilation callback.
     *
     * @param e BlueJ failed-compilation event
     */
    @Override
    public void compileFailed(CompileEvent e) {
        recorder.compileFailed(names(e), e.isUserGeneratedCompilation());
    }
}
