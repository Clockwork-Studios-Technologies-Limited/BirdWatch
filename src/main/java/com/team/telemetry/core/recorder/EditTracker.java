package com.team.telemetry.core.recorder;

import com.team.telemetry.core.parse.SourceScanner;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Jibril contributed this code: added the edit tracker state
/**
 * BlueJ's API has no keystroke listener, so the extension polls editor text and feeds snapshots here.
 * Differences between consecutive snapshots become EDIT events; newly seen methods become METHOD_DEFINED.
 */
public class EditTracker {
    private final TelemetryRecorder recorder;
    private final SourceScanner scanner;
    private final Map<String, String> lastText = new HashMap<>();
    private final Map<String, Set<String>> knownMethods = new HashMap<>();

    /**
     * Creates an editor snapshot tracker.
     *
     * @param recorder destination for detected edit and method events
     * @param scanner extracts method and constructor names from source text
     */
    public EditTracker(TelemetryRecorder recorder, SourceScanner scanner) {
        this.recorder = recorder;
        this.scanner = scanner;
    }

    // Jibril contributed this code: added snapshot guards and baseline capture
    /**
     * Compares a source snapshot with the previous snapshot for the same class.
     *
     * <p>The first snapshot establishes a baseline. Later changes record an edit and any newly
     * detected method names.</p>
     *
     * @param className class associated with the snapshot
     * @param text complete current editor text
     * @return {@code true} if a changed snapshot produced an edit event; {@code false} for a baseline,
     *         unchanged snapshot, or null argument
     */
    public synchronized boolean onSnapshot(String className, String text) {
        if (className == null || text == null) {
            return false;
        }
        String previous = lastText.put(className, text);
        Set<String> found = scanner.findMethodNames(text);
        Set<String> before = knownMethods.put(className, found);
        if (previous == null || previous.equals(text)) {
            return false; // first snapshot is only a baseline
        }
        // Jibril contributed this code: added diff measurement and EDIT recording
        int prefix = commonPrefix(previous, text);
        int suffix = commonSuffix(previous, text, prefix);
        int removed = previous.length() - prefix - suffix;
        int added = text.length() - prefix - suffix;
        recorder.edit(className, added, removed, countLines(text) - countLines(previous), text.length());
        // 34. Ihsan contributed this code: added new method detection
        for (String method : found) {
            if (before == null || !before.contains(method)) {
                recorder.methodDefined(className, method);
            }
        }
        return true;
    }

    // Jibril contributed this code: added per-class tracking state removal
    /**
     * Discards the saved text and method names for a class.
     *
     * @param className class whose tracking state should be removed
     */
    public synchronized void forget(String className) {
        lastText.remove(className);
        knownMethods.remove(className);
    }

    // Jibril contributed this code: added the common prefix and suffix diff helpers
    private static int commonPrefix(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private static int commonSuffix(String a, String b, int prefix) {
        int max = Math.min(a.length(), b.length()) - prefix;
        int i = 0;
        while (i < max && a.charAt(a.length() - 1 - i) == b.charAt(b.length() - 1 - i)) {
            i++;
        }
        return i;
    }

    // Jibril contributed this code: added line counting
    private static int countLines(String s) {
        int lines = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }
}
