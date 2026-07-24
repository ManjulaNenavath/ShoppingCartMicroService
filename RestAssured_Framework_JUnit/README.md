# REST Assured Framework — JUnit 5 variant

Same API tests as `RestAssured_Framework_OnlineStoreAPITesting`, rebuilt on **JUnit 5**
instead of TestNG. Kept side-by-side so you can compare the two runners on identical tests.

**REST Assured itself doesn't change** — it's just the HTTP client. Only the *runner glue*
differs: ordering, parametrization, lifecycle hooks, and the report listener.

## Run it
```bash
# start the 3 services first (see ../ecommerce-microservices)
cd RestAssured_Framework_JUnit
mvn clean test
```
Outputs: `reports/TestReport.pdf`, `logs/test_logging.txt`, `target/surefire-reports/`.
**29 tests** (User 9, Product 10 incl. 3 data-driven, Cart 7, Schema 3), all passing.

## What is identical (framework-agnostic, copied verbatim)
`routes/`, `payloads/PayloadManager` + the `src/test/resources/payloads/*.json` templates,
`utils/ConfigReader`, `Config.properties`, the `*Schema.json` contracts, and `testdata/`.
That's the point: a well-layered framework isolates the runner from everything else.

## What changed vs the TestNG version

| Concern | TestNG | JUnit 5 (here) |
|---|---|---|
| Test annotation | `@Test` (org.testng) | `@Test` (org.junit.jupiter) |
| Method order | `@Test(priority = n)` | `@TestMethodOrder(OrderAnnotation)` + `@Order(n)` |
| Shared instance state across methods | default | `@TestInstance(PER_CLASS)` (JUnit rebuilds per-method otherwise) |
| One-time setup | `@BeforeClass` | `@BeforeAll` |
| Assertion arg order | `assertEquals(actual, expected)` | `assertEquals(EXPECTED, actual)` ← flipped! |
| Data-driven | `@DataProvider` + `dataProviderClass` | `@ParameterizedTest` + `@MethodSource` |
| Suite / parallel config | `testng.xml` (`parallel="tests"`) | `junit-platform.properties` |
| Report hook (once at end) | `IReporter.generateReport` | `TestExecutionListener.testPlanExecutionFinished` |
| Listener registration | `<listener>` in `testng.xml` | `META-INF/services/...TestExecutionListener` (ServiceLoader) |

### The assertion-order gotcha (worth burning in)
TestNG: `assertEquals(response.statusCode(), 404)` — actual first.
JUnit:  `assertEquals(404, response.statusCode())` — **expected first**.
Get it backwards and the test still passes/fails correctly, but failure messages print
"expected/actual" swapped, which sends you debugging the wrong side. All JUnit tests here use
the correct `(expected, actual)` order.

## Reporting — PDF via a platform listener
`utils/PdfReporter` implements `TestExecutionListener`. It collects each test's result in
`executionFinished` and draws the PDF in `testPlanExecutionFinished` (fires once, after
everything). It's auto-registered by the ServiceLoader file under
`src/test/resources/META-INF/services/`. The PDF-drawing code (OpenPDF) is copied unchanged from
the TestNG reporter — only the result-collection callbacks differ.

## Parallel execution + a real surefire caveat
`junit-platform.properties` runs **classes concurrently, methods within a class sequentially** —
the exact behaviour of TestNG's `parallel="tests"`:
```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```
**Senior-tester note:** under concurrent classes, Maven Surefire's *per-class* counts in
`target/surefire-reports` can be misattributed (e.g. one class shows 20, another 3) — a known
Surefire threading limitation. The **grand total and pass/fail are always correct**, and the
**PDF report is accurate per-test** because our listener uses thread-safe counters. For clean
per-class Surefire numbers, run fully sequentially:
```bash
mvn test -Djunit.jupiter.execution.parallel.mode.classes.default=same_thread
```
This is a good lesson in itself: know your reporter's concurrency behaviour before trusting its
breakdown.
