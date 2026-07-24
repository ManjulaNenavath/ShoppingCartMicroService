package base;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * JUnit 5 equivalent of the TestNG BaseClass.
 *
 * @TestInstance(PER_CLASS): JUnit normally builds a NEW test instance per method,
 * which would wipe instance fields (e.g. the created userId) between chained steps.
 * PER_CLASS keeps one instance for the whole class - the behaviour TestNG gives by
 * default - and it also lets @BeforeAll be non-static. It is @Inherited, so every
 * subclass gets it.
 *
 * Thread-safety: junit-platform.properties runs classes concurrently, so several
 * @BeforeAll methods fire on different threads. RestAssured.filters(...) mutates
 * GLOBAL state, so we guard it and write it exactly ONCE (same fix as the TestNG
 * version - concurrent writes there caused a path-param NullPointerException).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private static final Object LOCK = new Object();
    private static volatile boolean configured = false;

    @BeforeAll
    void setup() throws Exception {
        synchronized (LOCK) {
            if (configured) {
                return;
            }
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream("logs/test_logging.txt", true);
            PrintStream log = new PrintStream(fos, true);
            RestAssured.filters(new RequestLoggingFilter(log), new ResponseLoggingFilter(log));
            configured = true;
        }
    }
}
