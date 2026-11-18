package com.team.telemetry.bluej;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import com.team.telemetry.TelemetryService;
import java.net.URL;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

// Ihsan contributed this code: added the extension entry point and background export executor
/**
 * Entry point named in the jar manifest (Main-Class). The only class that wires BlueJ to the telemetry core.
 *
 * BlueJ extension properties (stored in BlueJ's properties file) are "studentId", "outputDir"
 * (defaulting to the {@code bluej} directory under the user's home directory), and "idleSeconds"
 * (default 30).
 */
public class TelemetryExtension extends Extension {
    private static final Logger LOG = Logger.getLogger(TelemetryExtension.class.getName());
    private static final long POLL_MILLIS = 500;

    private TelemetryService service;
    private EditorPoller poller;
    private Path outputDir;
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "telemetry-export");
        t.setDaemon(true);
        return t;
    });

    // Ihsan contributed this code: added extension startup configuration and service creation
    /**
     * Reads extension properties, wires BlueJ listeners, and starts telemetry collection.
     *
     * @param bluej BlueJ host application
     */
    @Override
    public void startup(BlueJ bluej) {
        String studentId = bluej.getExtensionPropertyString("studentId", "STUDENT_ID_PLACEHOLDER");
        String configuredOutputDir = bluej.getExtensionPropertyString("outputDir",
                Path.of(System.getProperty("user.home"), "bluej").toString());
        String defaultOutputDir = Path.of(System.getProperty("user.home"), "bluej").toString();
        outputDir = Path.of(configuredOutputDir == null || configuredOutputDir.isBlank() ? defaultOutputDir : configuredOutputDir);
        long idleSeconds = parseLong(bluej.getExtensionPropertyString("idleSeconds", "30"), 30);
        if (idleSeconds <= 0) {
            idleSeconds = 30;
        }

        service = new TelemetryService(Clock.systemUTC(), studentId, Duration.ofSeconds(idleSeconds));
        service.start();

        // Ihsan contributed this code: added listener registration and editor polling start
        OpenPackageRegistry registry = new OpenPackageRegistry();
        bluej.addPackageListener(new BlueJPackageAdapter(service, registry, () -> exportAsync(false)));
        bluej.addClassListener(new BlueJClassAdapter(service));
        bluej.addCompileListener(new BlueJCompileAdapter(service.recorder()));
        bluej.addInvocationFinishedListener(new BlueJInvocationAdapter(service.recorder()));
        bluej.setMenuGenerator(new TelemetryMenu(() -> exportAsync(true)));

        poller = new EditorPoller(registry::snapshot, service.editTracker(), POLL_MILLIS);
        poller.start();
    }

    // Ihsan contributed this code: added background report export
    /**
     * Submits an export to the background executor so the JavaFX thread is not blocked.
     *
     * @param notifyUser whether to display the generated file paths on completion
     */
    private void exportAsync(boolean notifyUser) {
        exportExecutor.submit(() -> {
            try {
                TelemetryService.ExportResult result = service.export(outputDir);
                if (notifyUser) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,
                            "BirdWatch report saved:\n" + result.json() + "\n" + result.pdf(), ButtonType.OK).show());
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "BirdWatch export failed", e);
            }
        });
    }

    // 106. Ihsan contributed this code: added shutdown and final export
    /** Stops polling, ends the session, attempts a final export, and shuts down the executor. */
    @Override
    public void terminate() {
        if (poller != null) {
            poller.stop();
        }
        if (service != null) {
            service.end();
            try {
                service.export(outputDir); // final report at end of session
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Final BirdWatch export failed", e);
            }
        }
        exportExecutor.shutdown();
    }

    // 107. Ihsan contributed this code: added extension compatibility and descriptive metadata
    /**
     * Checks whether the BlueJ host exposes a supported Extensions API.
     *
     * @return {@code true} when the API major version is 3 or later
     */
    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    /** Returns this extension's version string. */
    @Override
    public String getVersion() { return "1.0.0"; }

    /** Returns the name displayed by BlueJ. */
    @Override
    public String getName() { return "BirdWatch"; }

    /** Returns the description displayed by BlueJ. */
    @Override
    public String getDescription() {
        return "BirdWatch records coding activity and exports a JSON file and PDF report";
    }

    /** Returns no project website because none is configured. */
    @Override
    public URL getURL() { return null; }

    // 108. Ihsan contributed this code: added safe numeric property parsing
    private static long parseLong(String s, long fallback) {
        if (s == null || s.isBlank()) {
            return fallback;
        }
        try {
            long value = Long.parseLong(s.trim());
            return value > 0 ? value : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
