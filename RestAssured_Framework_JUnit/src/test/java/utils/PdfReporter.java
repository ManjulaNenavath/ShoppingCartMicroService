package utils;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit 5 PDF report -> reports/TestReport.pdf
 *
 * This is the JUnit counterpart of the TestNG IReporter. It is a JUnit Platform
 * TestExecutionListener, auto-registered via
 *   src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener
 *
 * Lifecycle mapping:
 *   executionStarted   -> remember each test's start time
 *   executionFinished  -> record PASS/FAIL + duration + failure message
 *   executionSkipped   -> record SKIP
 *   testPlanExecutionFinished -> fires ONCE at the very end -> draw the PDF
 *
 * The drawing code (writePdf) is byte-for-byte the same idea as the TestNG version:
 * only the results-collection glue differs between the two runners.
 */
public class PdfReporter implements TestExecutionListener {

    private static final Color HEADER_BG = new Color(33, 47, 61);
    private static final Color PASS = new Color(30, 132, 73);
    private static final Color FAIL = new Color(176, 42, 55);
    private static final Color SKIP = new Color(183, 121, 31);
    private static final Color ZEBRA = new Color(245, 246, 248);

    private record Row(String name, String status, long ms, String details) {
    }

    private final Queue<Row> rows = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, Long> startNanos = new ConcurrentHashMap<>();
    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    @Override
    public void executionStarted(TestIdentifier id) {
        if (id.isTest()) {
            startNanos.put(id.getUniqueId(), System.nanoTime());
        }
    }

    @Override
    public void executionSkipped(TestIdentifier id, String reason) {
        if (id.isTest()) {
            skipped.incrementAndGet();
            rows.add(new Row(name(id), "SKIP", 0, reason == null ? "" : reason));
        }
    }

    @Override
    public void executionFinished(TestIdentifier id, TestExecutionResult result) {
        if (!id.isTest()) {
            return;
        }
        Long start = startNanos.remove(id.getUniqueId());
        long ms = start == null ? 0 : (System.nanoTime() - start) / 1_000_000;

        String status;
        String details = "";
        switch (result.getStatus()) {
            case SUCCESSFUL -> {
                status = "PASS";
                passed.incrementAndGet();
            }
            case ABORTED -> {
                status = "SKIP";
                skipped.incrementAndGet();
            }
            default -> {
                status = "FAIL";
                failed.incrementAndGet();
                details = result.getThrowable().map(Throwable::getMessage).orElse("");
            }
        }
        if (details != null && details.contains("\n")) {
            details = details.substring(0, details.indexOf('\n'));
        }
        if (details != null && details.length() > 160) {
            details = details.substring(0, 157) + "...";
        }
        rows.add(new Row(name(id), status, ms, details == null ? "" : details));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            List<Row> all = new ArrayList<>(rows);
            all.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
            writePdf(all, passed.get(), failed.get(), skipped.get());
        } catch (Exception e) {
            System.err.println("PDF report generation failed: " + e.getMessage());
        }
    }

    private String name(TestIdentifier id) {
        return id.getSource().map(s -> {
            if (s instanceof MethodSource ms) {
                return simple(ms.getClassName()) + "." + ms.getMethodName();
            }
            return id.getDisplayName();
        }).orElse(id.getDisplayName());
    }

    private String simple(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? fqcn : fqcn.substring(dot + 1);
    }

    // ---- identical layout to the TestNG PDF reporter ----

    private void writePdf(List<Row> rows, int passed, int failed, int skipped) throws Exception {
        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Document doc = new Document(PageSize.A4, 36, 36, 42, 36);
        PdfWriter.getInstance(doc, new FileOutputStream("reports/TestReport.pdf"));
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(33, 47, 61));
        Paragraph title = new Paragraph("Online Store API - Test Report (JUnit 5)", titleFont);
        title.setSpacingAfter(4);
        doc.add(title);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        doc.add(new Paragraph("Generated: " + ts + "   |   Framework: REST Assured + JUnit 5", metaFont));
        doc.add(new Paragraph("User: " + ConfigReader.get("user.baseUrl")
                + "   Product: " + ConfigReader.get("product.baseUrl")
                + "   Cart: " + ConfigReader.get("cart.baseUrl"), metaFont));
        doc.add(new Paragraph(" "));

        int total = passed + failed + skipped;
        double rate = total == 0 ? 0 : (passed * 100.0 / total);
        PdfPTable summary = new PdfPTable(5);
        summary.setWidthPercentage(100);
        summary.setWidths(new float[]{1.2f, 1f, 1f, 1f, 1.4f});
        summary.addCell(headerCell("Total"));
        summary.addCell(headerCell("Passed"));
        summary.addCell(headerCell("Failed"));
        summary.addCell(headerCell("Skipped"));
        summary.addCell(headerCell("Pass Rate"));
        summary.addCell(bodyCell(String.valueOf(total), Color.BLACK, false));
        summary.addCell(bodyCell(String.valueOf(passed), PASS, true));
        summary.addCell(bodyCell(String.valueOf(failed), failed > 0 ? FAIL : Color.BLACK, failed > 0));
        summary.addCell(bodyCell(String.valueOf(skipped), skipped > 0 ? SKIP : Color.BLACK, skipped > 0));
        summary.addCell(bodyCell(String.format("%.1f%%", rate), rate == 100 ? PASS : FAIL, true));
        doc.add(summary);
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.6f, 3.4f, 1f, 1.1f, 3.5f});
        for (String h : new String[]{"#", "Test", "Status", "Time (ms)", "Details"}) {
            table.addCell(headerCell(h));
        }

        int i = 1;
        for (Row row : rows) {
            boolean zebra = (i % 2 == 0);
            table.addCell(dataCell(String.valueOf(i), Color.BLACK, zebra, Element.ALIGN_CENTER));
            table.addCell(dataCell(row.name(), Color.BLACK, zebra, Element.ALIGN_LEFT));
            table.addCell(dataCell(row.status(), statusColor(row.status()), zebra, Element.ALIGN_CENTER));
            table.addCell(dataCell(String.valueOf(row.ms()), Color.BLACK, zebra, Element.ALIGN_RIGHT));
            table.addCell(dataCell(row.details(), Color.DARK_GRAY, zebra, Element.ALIGN_LEFT));
            i++;
        }
        doc.add(table);
        doc.close();
    }

    private Color statusColor(String status) {
        return switch (status) {
            case "PASS" -> PASS;
            case "FAIL" -> FAIL;
            default -> SKIP;
        };
    }

    private PdfPCell headerCell(String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell bodyCell(String text, Color color, boolean bold) {
        Font f = FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 11, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell dataCell(String text, Color color, boolean zebra, int align) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        if (zebra) {
            cell.setBackgroundColor(ZEBRA);
        }
        return cell;
    }
}
