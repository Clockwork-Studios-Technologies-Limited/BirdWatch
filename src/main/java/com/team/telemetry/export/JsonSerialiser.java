package com.team.telemetry.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.team.telemetry.analytics.ActivitySummary;
import com.team.telemetry.analytics.CompilationSummary;
import com.team.telemetry.analytics.TimelineEntry;
import com.team.telemetry.core.event.TelemetryEvent;
import com.team.telemetry.core.session.SessionMetadata;
import java.util.LinkedHashMap;
import java.util.Map;

// Ihsan contributed this code: added the JSON serialiser configuration
/** Pure translation of ReportData into the documented JSON schema (no file I/O). */
public class JsonSerialiser {
    /** Version number written to the root {@code schemaVersion} property. */
    public static final int SCHEMA_VERSION = 1;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    /**
     * Serializes report data as pretty-printed JSON.
     *
     * @param data report to serialize
     * @return JSON text in schema version {@link #SCHEMA_VERSION}
     */
    public String serialise(ReportData data) {
        return gson.toJson(toJson(data));
    }

    // Ihsan contributed this code: added the JSON root, session and summary sections
    /**
     * Maps report data to a JSON object containing session, summaries, timeline, and raw events.
     *
     * @param data report to map
     * @return JSON object conforming to the current schema
     */
    public JsonObject toJson(ReportData data) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        root.add("session", session(data.getSession()));

        JsonObject summary = new JsonObject();
        summary.add("activity", activity(data.getActivity()));
        summary.add("compilation", compilation(data.getCompilation()));
        root.add("summary", summary);

        // Ihsan contributed this code: added the JSON timeline section
        JsonArray timeline = new JsonArray();
        for (TimelineEntry t : data.getTimeline()) {
            JsonObject o = new JsonObject();
            o.addProperty("timestamp", t.timestamp().toString());
            o.addProperty("offsetSeconds", t.offsetSeconds());
            o.addProperty("type", t.type().name());
            o.addProperty("className", t.className());
            o.addProperty("methodName", t.methodName());
            timeline.add(o);
        }
        root.add("timeline", timeline);

        // Ihsan contributed this code: added the JSON raw events section
        JsonArray events = new JsonArray();
        for (TelemetryEvent e : data.getEvents()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", e.getId());
            o.addProperty("type", e.getType().name());
            o.addProperty("timestamp", e.getTimestamp().toString());
            o.add("data", gson.toJsonTree(new LinkedHashMap<>(e.getData())));
            events.add(o);
        }
        root.add("events", events);
        return root;
    }

    // Ihsan contributed this code: added the session JSON mapping
    private static JsonObject session(SessionMetadata s) {
        JsonObject o = new JsonObject();
        o.addProperty("studentId", s.getStudentId());
        o.addProperty("projectName", s.getProjectName());
        o.addProperty("startTime", s.getStartTime().toString());
        o.addProperty("endTime", s.getEndTime().toString());
        o.addProperty("durationSeconds", s.getDurationSeconds());
        o.addProperty("totalCompilationAttempts", s.getTotalCompilationAttempts());
        o.addProperty("successfulCompilations", s.getSuccessfulCompilations());
        o.addProperty("failedCompilations", s.getFailedCompilations());
        o.addProperty("totalEvents", s.getTotalEvents());
        return o;
    }

    // Ihsan contributed this code: added the activity JSON mapping
    private static JsonObject activity(ActivitySummary a) {
        JsonObject o = new JsonObject();
        o.addProperty("activeSeconds", a.activeMillis() / 1000);
        o.addProperty("idleSeconds", a.idleMillis() / 1000);
        o.addProperty("activeRatio", round(a.activeRatio()));
        o.addProperty("editCount", a.editCount());
        o.addProperty("charsAdded", a.charsAdded());
        o.addProperty("charsRemoved", a.charsRemoved());
        o.addProperty("charsPerActiveMinute", round(a.charsPerActiveMinute()));
        o.addProperty("editsPerActiveMinute", round(a.editsPerActiveMinute()));
        return o;
    }

    // Ihsan contributed this code: added the compilation JSON mapping
    private JsonObject compilation(CompilationSummary c) {
        JsonObject o = new JsonObject();
        o.addProperty("attempts", c.attempts());
        o.addProperty("succeeded", c.succeeded());
        o.addProperty("failed", c.failed());
        o.addProperty("warnings", c.warnings());
        o.addProperty("errors", c.errorCount());
        o.addProperty("successRate", round(c.successRate()));
        o.addProperty("errorsPerAttempt", round(c.errorsPerAttempt()));
        Map<String, Integer> freq = c.errorFrequency();
        o.add("errorFrequency", gson.toJsonTree(new LinkedHashMap<>(freq)));
        return o;
    }

    // Ihsan contributed this code: added two-decimal rounding
    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
