package com.team.telemetry.bluej;

import bluej.extensions2.BPackage;
import bluej.extensions2.MenuGenerator;
import javafx.scene.control.MenuItem;

// Ihsan contributed this code: added the Export BirdWatch Report menu item
/** Adds Tools > "Export BirdWatch Report". A fresh MenuItem is returned on every call (JavaFX requirement). */
public class TelemetryMenu extends MenuGenerator {
    private final Runnable exportAction;

    /**
     * Creates the menu generator.
     *
     * @param exportAction action invoked when the menu item is selected
     */
    public TelemetryMenu(Runnable exportAction) {
        this.exportAction = exportAction;
    }

    /**
     * Creates the Tools menu item that triggers the configured export action.
     *
     * @param bPackage BlueJ package for which the menu is being requested
     * @return a new export menu item
     */
    @Override
    public MenuItem getToolsMenuItem(BPackage bPackage) {
        MenuItem item = new MenuItem("Export BirdWatch Report");
        item.setOnAction(event -> exportAction.run());
        return item;
    }
}
