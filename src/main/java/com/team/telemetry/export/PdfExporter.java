package com.team.telemetry.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.team.telemetry.analytics.ActivitySummary;
import com.team.telemetry.analytics.CompilationSummary;
import com.team.telemetry.analytics.TimelineEntry;
import com.team.telemetry.core.session.SessionMetadata;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// Gabriella contributed this code: added the PDF styling constants and fonts
/** Renders the end-of-session PDF: session summary, active vs idle time, compile errors, timeline. */
public class PdfExporter implements Exporter {
    static final int MAX_TIMELINE_ROWS = 100;
    private static final Color HEADER_BG = new Color(226, 234, 246);
    private static final Color ACTIVE = new Color(46, 160, 67);
    private static final Color IDLE = new Color(190, 190, 190);
    private static final Color ERROR_BAR = new Color(207, 60, 60);

    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, Color.BLACK);
    private final Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Color.BLACK);
    private final Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, Color.BLACK);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, Color.BLACK);

    // Gabriella contributed this code: added PDF file export
    /** Returns {@code "pdf"}. */
    @Override
    public String fileExtension() { return "pdf"; }

    /**
     * Writes a PDF report to the target, creating parent directories as needed.
     *
     * @param data report to render
     * @param target destination file
     * @throws IOException if the file cannot be written or the PDF cannot be generated
     */
    @Override
    public void export(ReportData data, Path target) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream out = Files.newOutputStream(target)) {
            write(data, out);
        }
    }

    // Gabriella contributed this code: added PDF document assembly
    /**
     * Renders report sections to the supplied output stream.
     *
     * @param data report to render
     * @param out destination stream; this method does not close it
     * @throws IOException if PDF generation fails
     */
    public void write(ReportData data, OutputStream out) throws IOException {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("BirdWatch Report", titleFont));
            document.add(spacer());
            addSession(document, data.getSession());
            addActivity(document, data.getActivity());
            addCompilation(document, data.getCompilation());
            addTimeline(document, data.getTimeline());
        } catch (DocumentException e) {
            throw new IOException("Could not build PDF report", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    // Gabriella contributed this code: added the PDF session summary section
    private void addSession(Document doc, SessionMetadata s) throws DocumentException {
        doc.add(new Paragraph("Session", h2Font));
        PdfPTable t = table(30f, 70f);
        row(t, "Student ID", s.getStudentId());
        row(t, "Project", s.getProjectName().isEmpty() ? "-" : s.getProjectName());
        row(t, "Start", s.getStartTime().toString());
        row(t, "End", s.getEndTime().toString());
        row(t, "Duration", DurationFormatter.format(s.getDurationSeconds() * 1000));
        row(t, "Compilation attempts", String.valueOf(s.getTotalCompilationAttempts()));
        row(t, "Total events", String.valueOf(s.getTotalEvents()));
        doc.add(t);
        doc.add(spacer());
    }

    // Gabriella contributed this code: added the PDF active and idle statistics table
    private void addActivity(Document doc, ActivitySummary a) throws DocumentException {
        doc.add(new Paragraph("Active vs idle time", h2Font));
        PdfPTable t = table(30f, 70f);
        row(t, "Active time", DurationFormatter.format(a.activeMillis()));
        row(t, "Idle time", DurationFormatter.format(a.idleMillis()));
        row(t, "Active ratio", String.format("%.0f%%", a.activeRatio() * 100));
        row(t, "Edits recorded", String.valueOf(a.editCount()));
        row(t, "Characters added / removed", a.charsAdded() + " / " + a.charsRemoved());
        row(t, "Typing cadence", String.format("%.1f chars per active minute", a.charsPerActiveMinute()));
        row(t, "Edit frequency", String.format("%.1f edits per active minute", a.editsPerActiveMinute()));
        doc.add(t);
        // Gabriella contributed this code: added the PDF active versus idle bar
        if (a.totalMillis() > 0) {
            doc.add(new Paragraph("Green = active, grey = idle", bodyFont));
            PdfPTable wrapper = new PdfPTable(1);
            wrapper.setWidthPercentage(100);
            wrapper.addCell(barCell(a.activeRatio(), ACTIVE, IDLE));
            doc.add(wrapper);
        } else {
            doc.add(new Paragraph("No activity was recorded.", bodyFont));
        }
        doc.add(spacer());
    }

    // Gabriella contributed this code: added the PDF compilation summary table
    private void addCompilation(Document doc, CompilationSummary c) throws DocumentException {
        doc.add(new Paragraph("Compilation", h2Font));
        PdfPTable t = table(30f, 70f);
        row(t, "Attempts", String.valueOf(c.attempts()));
        row(t, "Succeeded / failed", c.succeeded() + " / " + c.failed());
        row(t, "Success rate", String.format("%.0f%%", c.successRate() * 100));
        row(t, "Errors / warnings", c.errorCount() + " / " + c.warnings());
        doc.add(t);
        doc.add(spacer());

        // Gabriella contributed this code: added the PDF compilation error frequency table
        doc.add(new Paragraph("Compilation error frequency", h2Font));
        Map<String, Integer> freq = c.errorFrequency();
        if (freq.isEmpty()) {
            doc.add(new Paragraph("No compilation errors were recorded.", bodyFont));
        } else {
            int max = freq.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            PdfPTable ft = new PdfPTable(new float[] {50f, 10f, 40f});
            ft.setWidthPercentage(100);
            ft.addCell(headerCell("Error code"));
            ft.addCell(headerCell("Count"));
            ft.addCell(headerCell("Share"));
            for (Map.Entry<String, Integer> e : freq.entrySet()) {
                ft.addCell(cell(e.getKey(), bodyFont));
                ft.addCell(cell(String.valueOf(e.getValue()), bodyFont));
                ft.addCell(barCell((double) e.getValue() / max, ERROR_BAR, null));
            }
            doc.add(ft);
        }
        doc.add(spacer());
    }

    // Gabriella contributed this code: added the PDF class and method progression table
    private void addTimeline(Document doc, List<TimelineEntry> timeline) throws DocumentException {
        doc.add(new Paragraph("Class and method progression", h2Font));
        if (timeline.isEmpty()) {
            doc.add(new Paragraph("No class creations or method definitions were recorded.", bodyFont));
            return;
        }
        PdfPTable t = new PdfPTable(new float[] {15f, 20f, 30f, 35f});
        t.setWidthPercentage(100);
        t.addCell(headerCell("Offset"));
        t.addCell(headerCell("Event"));
        t.addCell(headerCell("Class"));
        t.addCell(headerCell("Method"));
        int shown = Math.min(timeline.size(), MAX_TIMELINE_ROWS);
        for (int i = 0; i < shown; i++) {
            TimelineEntry e = timeline.get(i);
            t.addCell(cell(DurationFormatter.format(e.offsetSeconds() * 1000), bodyFont));
            t.addCell(cell(e.type().name(), bodyFont));
            t.addCell(cell(e.className() == null ? "-" : e.className(), bodyFont));
            t.addCell(cell(e.methodName() == null ? "-" : e.methodName(), bodyFont));
        }
        doc.add(t);
        if (timeline.size() > shown) {
            doc.add(new Paragraph("... and " + (timeline.size() - shown) + " more (see the JSON export).", bodyFont));
        }
    }

    // Gabriella contributed this code: added PDF layout helpers for spacing, tables and label rows
    // ---- helpers ----
    private static Paragraph spacer() {
        return new Paragraph(" ");
    }

    private PdfPTable table(float... widths) throws DocumentException {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        return t;
    }

    private void row(PdfPTable t, String label, String value) {
        PdfPCell l = cell(label, boldFont);
        l.setBackgroundColor(HEADER_BG);
        t.addCell(l);
        t.addCell(cell(value, bodyFont));
    }

    // Gabriella contributed this code: added PDF header and text cell helpers
    private PdfPCell headerCell(String text) {
        PdfPCell c = cell(text, boldFont);
        c.setBackgroundColor(HEADER_BG);
        return c;
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setPadding(4f);
        return c;
    }

    // Gabriella contributed this code: added the PDF proportional bar
    /** A horizontal bar built from a nested two-cell table, so no low-level drawing is needed. */
    private static PdfPCell barCell(double fraction, Color fill, Color rest) {
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, fraction)) * 100);
        PdfPTable bar;
        try {
            if (filled <= 0 || filled >= 100) {
                bar = new PdfPTable(1);
                bar.addCell(solid(filled >= 100 ? fill : rest));
            } else {
                bar = new PdfPTable(new float[] {filled, 100 - filled});
                bar.addCell(solid(fill));
                bar.addCell(solid(rest));
            }
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        bar.setWidthPercentage(100);
        PdfPCell wrapper = new PdfPCell(bar);
        wrapper.setPadding(4f);
        wrapper.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return wrapper;
    }

    // Gabriella contributed this code: added the solid colour bar segment
    private static PdfPCell solid(Color color) {
        PdfPCell c = new PdfPCell(new Phrase(" "));
        c.setBorder(Rectangle.NO_BORDER);
        c.setFixedHeight(10f);
        if (color != null) {
            c.setBackgroundColor(color);
        }
        return c;
    }
}
