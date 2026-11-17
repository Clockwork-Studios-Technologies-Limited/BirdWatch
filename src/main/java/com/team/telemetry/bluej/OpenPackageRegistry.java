package com.team.telemetry.bluej;

import bluej.extensions2.BPackage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Jibril contributed this code: added open package tracking
/** Remembers which BlueJ packages are open so the editor poller knows where to look. */
public class OpenPackageRegistry {
    private final Set<BPackage> packages = ConcurrentHashMap.newKeySet();

    /**
     * Adds a package to the open-package set.
     *
     * @param p package to add
     */
    public void add(BPackage p) { packages.add(p); }
    /**
     * Removes a package from the open-package set.
     *
     * @param p package to remove
     */
    public void remove(BPackage p) { packages.remove(p); }
    /**
     * Checks whether any packages are registered as open.
     *
     * @return {@code true} when the registry is empty
     */
    public boolean isEmpty() { return packages.isEmpty(); }
    /**
     * Copies the currently registered packages.
     *
     * @return a detached snapshot of the open-package set
     */
    public Collection<BPackage> snapshot() { return new ArrayList<>(packages); }
}
