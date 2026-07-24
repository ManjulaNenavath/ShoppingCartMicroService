package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that produces an interactive Extent (Spark) HTML report.
 *
 * Registered in testng.xml as a <listener>. On every test start/pass/fail/skip,
 * TestNG calls the matching method here and we record it. Output lands in
 * /reports/ExtentReport.html after the run.
 *
 * ThreadLocal<ExtentTest> keeps per-test nodes correct even under parallel
 * execution (testng.xml uses parallel="tests").
 */
public class ExtentReporter implements ITestListener {

    private ExtentReports extent;
    private final ThreadLocal<ExtentTest> current = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/ExtentReport.html");
        spark.config().setReportName("Online Store API - REST Assured");
        spark.config().setDocumentTitle("API Automation Report");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework", "REST Assured + TestNG");
        extent.setSystemInfo("User Service", ConfigReader.get("user.baseUrl"));
        extent.setSystemInfo("Product Service", ConfigReader.get("product.baseUrl"));
        extent.setSystemInfo("Cart Service", ConfigReader.get("cart.baseUrl"));
    }

    @Override
    public void onTestStart(ITestResult result) {
        current.set(extent.createTest(result.getMethod().getMethodName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        current.get().log(Status.PASS, "Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        current.get().log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        current.get().log(Status.SKIP, "Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
