# BirdWatch

A BlueJ 5 extension (Extensions API 3.x) that records coding telemetry and exports a time-stamped
JSON file and a PDF report.

## Build
Use JDK 17 and Maven 3.x. The Maven build requires the BlueJ 5 Extensions API at
`lib/bluej.jar` (copy it from your BlueJ installation if it is not already present); the dependency
is declared with system scope in `pom.xml`. Set `javafx.version` to the JavaFX version bundled with
your BlueJ installation.

Run `mvn clean verify` to compile the extension, run the tests, enforce the JaCoCo line-coverage
gate, and create `target/birdwatch-1.0.0.jar`. Generate the API documentation with
`mvn clean javadoc:javadoc`; the site is written to `target/reports/apidocs/`.

## Install
Copy the jar into `<BlueJ>/lib/extensions2/` or `~/.bluej/extensions2/` and restart BlueJ.
Reports go to the `bluej` directory under your home folder by default (for example,
`%USERPROFILE%\bluej` on Windows or `~/bluej` on macOS/Linux), or to the configured `outputDir`,
when **Tools > Export BirdWatch Report** is selected, when the last package closes, and when BlueJ
exits.

Optional BlueJ extension properties: `studentId`, `outputDir`, `idleSeconds` (default 30).

## Design
| Pattern | Where |
|---|---|
| Observer | `EventBus` / `EventSubscriber` |
| Adapter | `bluej/BlueJ*Adapter` |
| Factory | `TelemetryEventFactory` |
| Strategy | `Exporter` (JSON/PDF), `IdleDetectionStrategy` |
| Builder | `SessionMetadata.Builder`, `ReportData.Builder` |
| Facade | `TelemetryService` |

Only `com.team.telemetry.bluej` imports BlueJ classes; everything else is plain Java and unit tested.
JaCoCo enforces 75% line coverage (the BlueJ adapter package is excluded).

## Known API limits
- No keystroke listener: typing is detected by polling open editors every 500 ms (`EditorPoller`).
- No compile error codes: derived from the message (`CompileErrorClassifier`).
- No project/class creation event: creation is inferred from the first class event for an unseen class.
- No console-output hook: execution events carry the invocation result summary only.
