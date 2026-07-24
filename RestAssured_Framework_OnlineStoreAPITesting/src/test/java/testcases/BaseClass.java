package testcases;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Common setup for every test class.
 *
 * We attach RequestLoggingFilter + ResponseLoggingFilter so the FULL request and
 * response of every call are written to logs/test_logging.txt. When a test fails
 * at 2am, that file is the first place a senior tester looks - it shows the exact
 * payload sent and body received, no guesswork.
 *
 * THREAD-SAFETY (important): testng.xml runs parallel="tests", so four @BeforeClass
 * methods fire on four threads. RestAssured.filters(...) mutates GLOBAL static state.
 * Letting several threads write it concurrently corrupts RestAssured's internals and
 * throws a NullPointerException deep in request handling. So we configure exactly
 * ONCE behind a lock; the other threads block until it is done, then skip it.
 */
public class BaseClass {

    private static final Object LOCK = new Object();
    private static volatile boolean configured = false;

    @BeforeClass
    public void setup() throws Exception {
        synchronized (LOCK) {
            if (configured) {
                return;
            }

            // Ensure the logs directory exists (FileOutputStream will not create it).
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // append=true so re-runs and multiple classes don't truncate the log.
            FileOutputStream fos = new FileOutputStream("logs/test_logging.txt", true);
            PrintStream log = new PrintStream(fos, true);

            RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter(log);
            ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter(log);

            // Single global write, performed once, before any parallel test runs.
            RestAssured.filters(requestLoggingFilter, responseLoggingFilter);

            configured = true;
        }
    }
}
