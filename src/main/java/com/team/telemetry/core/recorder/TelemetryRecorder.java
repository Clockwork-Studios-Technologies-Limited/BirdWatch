package com.team.telemetry.core.recorder;

import com.team.telemetry.core.bus.EventBus;
import com.team.telemetry.core.event.EventType;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.event.TelemetryEventFactory;
import com.team.telemetry.core.parse.CompileErrorClassifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Ihsan contributed this code: added the recorder entry point that publishes events onto the bus
/**
 * BlueJ-independent entry point for everything the plugin observes. The BlueJ adapters translate
 * BlueJ's events into plain values and call these methods, so all mapping logic is unit-testable.
 */
public class TelemetryRecorder {
    static final int MAX_RESULT_LENGTH = 200;

    private final TelemetryEventFactory factory;
    private final EventBus bus;
    private final CompileErrorClassifier classifier;
    private final Set<String> knownClasses = ConcurrentHashMap.newKeySet();

    /**
     * Creates a recorder that sends events through the supplied factory and bus.
     *
     * @param factory creates timestamped events
     * @param bus publishes created events
     * @param classifier derives codes for compiler messages
     */
    public TelemetryRecorder(TelemetryEventFactory factory, EventBus bus, CompileErrorClassifier classifier) {
        this.factory = factory;
        this.bus = bus;
        this.classifier = classifier;
    }

    private void emit(EventType type, Object... keyValues) {
        TelemetryEvent event = factory.of(type, keyValues);
        bus.publish(event);
    }

    // Ihsan contributed this code: added session and project lifecycle recording
    // ---- session / project ----
    /**
     * Records the beginning of a session with its student identifier.
     *
     * @param studentId student identifier associated with the session
     */
    public void sessionStarted(String studentId) { emit(EventType.SESSION_STARTED, "studentId", studentId); }
    /** Records the end of the current session. */
    public void sessionEnded() { emit(EventType.SESSION_ENDED); }

    /**
     * Records that a project package was opened.
     *
     * @param project project name
     * @param packageName opened package name
     */
    public void projectOpened(String project, String packageName) {
        emit(EventType.PROJECT_OPENED, "project", project, "package", packageName);
    }

    /**
     * Records that a project package is closing.
     *
     * @param project project name
     * @param packageName closing package name
     */
    public void projectClosing(String project, String packageName) {
        emit(EventType.PROJECT_CLOSED, "project", project, "package", packageName);
    }

    // Ihsan contributed this code: added class lifecycle recording
    // ---- classes ----
    /**
     * Marks classes present at project open as pre-existing.
     *
     * @param classNames names of classes that should not be inferred as student-created
     */
    public void registerExistingClasses(Collection<String> classNames) {
        knownClasses.addAll(classNames);
    }

    /**
     * Records a class state update and infers creation the first time a class is observed.
     *
     * @param className class name
     * @param compiled whether BlueJ reports the class compiled
     * @param hasError whether BlueJ reports an error for the class
     */
    public void classStateChanged(String className, boolean compiled, boolean hasError) {
        if (knownClasses.add(className)) {
            emit(EventType.CLASS_CREATED, "className", className);
        }
        emit(EventType.CLASS_STATE_CHANGED, "className", className, "compiled", compiled, "hasError", hasError);
    }

    /**
     * Records a class rename and updates the known-class set.
     *
     * @param oldName prior class name
     * @param newName replacement class name
     */
    public void classRenamed(String oldName, String newName) {
        knownClasses.remove(oldName);
        knownClasses.add(newName);
        emit(EventType.CLASS_RENAMED, "oldName", oldName, "newName", newName);
    }

    /**
     * Records class removal and removes it from the known-class set.
     *
     * @param className removed class name
     */
    public void classRemoved(String className) {
        knownClasses.remove(className);
        emit(EventType.CLASS_REMOVED, "className", className);
    }

    // Ihsan contributed this code: added edit and method-definition recording
    // ---- editor ----
    /**
     * Records aggregate differences between two editor snapshots.
     *
     * @param className edited class
     * @param charsAdded number of inserted characters
     * @param charsRemoved number of removed characters
     * @param lineDelta change in line count
     * @param totalChars current source length in characters
     */
    public void edit(String className, int charsAdded, int charsRemoved, int lineDelta, int totalChars) {
        emit(EventType.EDIT, "className", className, "charsAdded", charsAdded, "charsRemoved", charsRemoved,
                "lineDelta", lineDelta, "totalChars", totalChars);
    }

    /**
     * Records detection of a method or constructor not present in the prior snapshot.
     *
     * @param className owning class
     * @param methodName detected declaration name
     */
    public void methodDefined(String className, String methodName) {
        emit(EventType.METHOD_DEFINED, "className", className, "methodName", methodName);
    }

    // Ihsan contributed this code: added compilation event recording
    // ---- compilation ----
    /**
     * Records the start of a compilation.
     *
     * @param files source file names involved, copied before recording
     * @param userGenerated whether the compilation resulted from a user action
     */
    public void compileStarted(List<String> files, boolean userGenerated) {
        emit(EventType.COMPILE_STARTED, "files", copy(files), "userGenerated", userGenerated);
    }

    /**
     * Records a compiler error and its derived code.
     *
     * @param files source file names involved
     * @param line reported source line
     * @param message compiler message used to derive a code
     * @param userGenerated whether the compilation resulted from a user action
     */
    public void compileError(List<String> files, int line, String message, boolean userGenerated) {
        emit(EventType.COMPILE_ERROR, "files", copy(files), "line", line, "message", message,
                "code", classifier.classify(message), "userGenerated", userGenerated);
    }

    /**
     * Records a compiler warning.
     *
     * @param files source file names involved
     * @param line reported source line
     * @param message compiler warning text
     * @param userGenerated whether the compilation resulted from a user action
     */
    public void compileWarning(List<String> files, int line, String message, boolean userGenerated) {
        emit(EventType.COMPILE_WARNING, "files", copy(files), "line", line, "message", message,
                "userGenerated", userGenerated);
    }

    /**
     * Records successful compilation completion.
     *
     * @param files source file names involved
     * @param userGenerated whether the compilation resulted from a user action
     */
    public void compileSucceeded(List<String> files, boolean userGenerated) {
        emit(EventType.COMPILE_SUCCEEDED, "files", copy(files), "userGenerated", userGenerated);
    }

    /**
     * Records unsuccessful compilation completion.
     *
     * @param files source file names involved
     * @param userGenerated whether the compilation resulted from a user action
     */
    public void compileFailed(List<String> files, boolean userGenerated) {
        emit(EventType.COMPILE_FAILED, "files", copy(files), "userGenerated", userGenerated);
    }

    // Ihsan contributed this code: added execution result recording with truncation
    // ---- execution ----
    /**
     * Records the completion of a method or constructor invocation.
     *
     * <p>The exit type selects the execution event category, and results longer than 200 characters
     * are truncated.</p>
     *
     * @param className invoked class
     * @param objectName target object name
     * @param methodName invoked method, or null for a constructor
     * @param exitType name of a BlueJ {@code InvocationFinishedEvent.EventType} constant
     * @param parameters invocation arguments
     * @param result textual result, if any
     */
    public void invocationFinished(String className, String objectName, String methodName, String exitType,
                                   List<String> parameters, String result) {
        EventType type = switch (exitType == null ? "" : exitType) {
            case "EXCEPTION_EXIT" -> EventType.EXECUTION_EXCEPTION;
            case "TERMINATED_EXIT" -> EventType.EXECUTION_TERMINATED;
            default -> EventType.EXECUTION_FINISHED;
        };
        String shortResult = result == null ? null
                : (result.length() > MAX_RESULT_LENGTH ? result.substring(0, MAX_RESULT_LENGTH) + "..." : result);
        emit(type, "className", className, "objectName", objectName,
                "methodName", methodName == null ? "<constructor>" : methodName,
                "exit", exitType, "parameters", copy(parameters), "result", shortResult);
    }

    // Ihsan contributed this code: added a null-safe list copy helper
    private static List<String> copy(List<String> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }
}
