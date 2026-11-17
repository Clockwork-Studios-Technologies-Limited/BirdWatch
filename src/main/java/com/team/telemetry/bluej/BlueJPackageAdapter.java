package com.team.telemetry.bluej;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;
import com.team.telemetry.TelemetryService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Jibril contributed this code: added package adapter setup
/** Adapter pattern: BlueJ PackageListener -> TelemetryRecorder calls. */
public class BlueJPackageAdapter implements PackageListener {
    private static final Logger LOG = Logger.getLogger(BlueJPackageAdapter.class.getName());

    private final TelemetryService service;
    private final OpenPackageRegistry registry;
    private final Runnable onLastPackageClosed;

    /**
     * Creates a package listener for package lifecycle events.
     *
     * @param service telemetry service receiving project events and exporting reports
     * @param registry registry of currently open packages
     * @param onLastPackageClosed action run when a closing package leaves the registry empty
     */
    public BlueJPackageAdapter(TelemetryService service, OpenPackageRegistry registry, Runnable onLastPackageClosed) {
        this.service = service;
        this.registry = registry;
        this.onLastPackageClosed = onLastPackageClosed;
    }

    // Jibril contributed this code: added project open handling
    /**
     * Records the opened project/package, registers existing classes, and tracks the package.
     *
     * @param event BlueJ package-open event
     */
    @Override
    public void packageOpened(PackageEvent event) {
        try {
            BPackage pkg = event.getPackage();
            String project = pkg.getProject().getName();
            service.setProjectName(project);
            service.recorder().projectOpened(project, pkg.getName());
            List<String> existing = new ArrayList<>();
            for (BClass c : pkg.getClasses()) {
                existing.add(c.getName());
            }
            service.recorder().registerExistingClasses(existing);
            registry.add(pkg);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "packageOpened failed", e);
        }
    }

    // Jibril contributed this code: added project close handling and last-package export trigger
    /**
     * Records package closure, removes it from the registry, and runs the close action when empty.
     *
     * @param event BlueJ package-closing event
     */
    @Override
    public void packageClosing(PackageEvent event) {
        try {
            BPackage pkg = event.getPackage();
            service.recorder().projectClosing(pkg.getProject().getName(), pkg.getName());
            registry.remove(pkg);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "packageClosing failed", e);
        }
        if (registry.isEmpty()) {
            onLastPackageClosed.run();
        }
    }
}
