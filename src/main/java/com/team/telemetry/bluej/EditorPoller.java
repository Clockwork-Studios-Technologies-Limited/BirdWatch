package com.team.telemetry.bluej;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.editor.TextLocation;
import com.team.telemetry.core.recorder.EditTracker;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Gabriella contributed this code: added the editor poller setup
/**
 * The extensions API has no keystroke/document listener, so open Java editors are polled and their text
 * is handed to the (BlueJ-independent) EditTracker, which turns differences into EDIT events.
 */
public class EditorPoller {
    private static final Logger LOG = Logger.getLogger(EditorPoller.class.getName());

    private final Supplier<Collection<BPackage>> packages;
    private final EditTracker tracker;
    private final long periodMillis;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "telemetry-editor-poller");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean failureLogged;

    /**
     * Creates a poller that snapshots editors from the supplied open-package collection.
     *
     * @param packages supplies the packages to inspect on each pass
     * @param tracker receives editor snapshots
     * @param periodMillis delay in milliseconds between polling passes
     */
    public EditorPoller(Supplier<Collection<BPackage>> packages, EditTracker tracker, long periodMillis) {
        this.packages = packages;
        this.tracker = tracker;
        this.periodMillis = periodMillis;
    }

    // Gabriella contributed this code: added poller start and stop
    /** Schedules editor polling at the configured fixed delay. */
    public void start() {
        executor.scheduleWithFixedDelay(this::pollOnce, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    /** Stops scheduled polling and interrupts any current pass. */
    public void stop() {
        executor.shutdownNow();
    }

    // Gabriella contributed this code: added the editor polling pass
    void pollOnce() {
        for (BPackage pkg : packages.get()) {
            try {
                for (BClass cls : pkg.getClasses()) {
                    JavaEditor editor = cls.getJavaEditorIfOpen();
                    if (editor != null) {
                        tracker.onSnapshot(cls.getName(), readText(editor));
                    }
                }
            } catch (Exception e) {
                if (!failureLogged) {
                    failureLogged = true;
                    LOG.log(Level.WARNING, "Editor polling failed (logged once)", e);
                }
            }
        }
    }

    // Gabriella contributed this code: added editor text reading
    private static String readText(JavaEditor editor) {
        TextLocation end = editor.getTextLocationFromOffset(editor.getTextLength());
        return editor.getText(new TextLocation(0, 0), end);
    }
}
