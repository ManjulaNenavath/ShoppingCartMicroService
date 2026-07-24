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
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a PDF summary of the whole test run: reports/TestReport.pdf
 *
 * WHY IReporter (not ITestListener)? testng.xml runs parallel="tests" with four
 * <test> blocks. ITestListener.onFinish fires once PER block, so it can only see
 * that block's results. IReporter.generateReport fires exactly ONCE at the very end
 * with every suite/context, so the PDF aggregates all tests correctly.
 */
public class PdfReporter implements IReporter {

    // Brand-ish palette
    private static final Color HEADER_BG = new Color(33, 47, 61);   // dark slate
    private static final Color PASS = new Color(30, 132, 73);       // green
    private static final Color FAIL = new Color(176, 42, 55);       // red
    private static final Color SKIP = new Color(183, 121, 31);      // amber
    private static final Color ZEBRA = new Color(245, 246, 248);    // light row

    private record Row(String name, String status, long ms, String details) {
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<Row> rows = new ArrayList<>();
        int passed = 0, failed = 0, skipped = 0;

        for (ISuite suite : suites) {
            for (ISuiteResult sr : suite.getResults().values()) {
                ITestContext ctx = sr.getTestContext();
                passed += ctx.getPassedTests().size();
                failed += ctx.getFailedTests().size();
                skipped += ctx.getSkippedTests().size();
                collect(rows, ctx.getPassedTests(), "PASS");
                collect(rows, ctx.getFailedTests(), "FAIL");
                collect(rows, ctx.getSkippedTests(), "SKIP");
            }
        }
        rows.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));

        try {
            writePdf(rows, passed, failed, skipped);
        } catch (Exception e) {
            System.err.println("PDF report generation failed: " + e.getMessage());
        }
    }

    private void collect(List<Row> rows, IResultMap map, String status) {
        for (ITestResult r : map.getAllResults()) {
            String name = r.getTestClass().getRealClass().getSimpleName() + "." + r.getMethod().getMethodName();
            long ms = r.getEndMillis() - r.getStartMillis();
            String details = "";
            if (r.getThrowable() != null) {
                details = r.getThrowable().getMessage();
                if (details != null && details.contains("\n")) {
                    details = details.substring(0, details.indexOf('\n'));
                }
                if (details != null && details.length() > 160) {
                    details = details.substring(0, 157) + "...";
                }
            }
            rows.add(new Row(name, status, ms, details == null ? "" : details));
        }
    }

    private void writePdf(List<Row> rows, int passed, int failed, int skipped) throws Exception {
        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Document doc = new Document(PageSize.A4, 36, 36, 42, 36);
        PdfWriter.getInstance(doc, new FileOutputStream("reports/TestReport.pdf"));
        doc.open();

        // ---- Title ----
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(33, 47, 61));
        Paragraph title = new Paragraph("Online Store API - Test Report", titleFont);
        title.setSpacingAfter(4);
        doc.add(title);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        doc.add(new Paragraph("Generated: " + ts + "   |   Framework: REST Assured + TestNG", metaFont));
        doc.add(new Paragraph("User: " + ConfigReader.get("user.baseUrl")
                + "   Product: " + ConfigReader.get("product.baseUrl")
                + "   Cart: " + ConfigReader.get("cart.baseUrl"), metaFont));
        doc.add(new Paragraph(" "));

        // ---- Summary table ----
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

        // ---- Detailed results ----
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
