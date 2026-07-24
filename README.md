# ShoppingCart Microservices + REST Assured Test Framework

A complete, runnable learning project for **API test automation with REST Assured** against a
real **Spring Boot microservices** backend. Built to be studied, not just run — the code is
commented the way a senior tester would explain it to someone joining the team.

```
D:\RestAssured\
├── ecommerce-microservices\                 # The system under test (SUT)
│   ├── user-service\        (port 8081)
│   ├── product-service\     (port 8082)
│   ├── cart-service\        (port 8083)
│   ├── docker-compose.yml
│   └── render.yaml                           # one-click cloud deploy
└── RestAssured_Framework_OnlineStoreAPITesting\   # The test framework
    ├── src/test/java/{routes,payloads,testcases,utils}
    ├── src/test/resources/
    │   ├── payloads/*.json          # request-body TEMPLATES (with {{placeholders}})
    │   ├── Config.properties
    │   └── *Schema.json             # response contract schemas
    ├── testdata/{User,Product,Cart}.json
    └── testng.xml
```

---

## Part 0 — What you are testing (the mental model)

Three independent services, each with its own database (H2 in-memory), each on its own port.
This is the essence of microservices: **no shared database, no in-process calls — everything
crosses the network as HTTP + JSON.** That is *why* API testing matters so much here: the
contract between services (and between a service and its clients) is the product.

| Service | Port | Base path | Responsibility |
|---|---|---|---|
| user-service | 8081 | `/api/users` | register, login, CRUD user |
| product-service | 8082 | `/api/products` | catalog CRUD (seeded with 3 products) |
| cart-service | 8083 | `/api/cart` | per-user cart, add/remove/clear items |

### Endpoint contract (what the tests assert)

**User** (`http://localhost:8081`)
| Method | Path | Success | Failure cases |
|---|---|---|---|
| POST | `/api/users/register` | 201 | 409 duplicate, 400 invalid |
| POST | `/api/users/login` | 200 (+token) | 401 bad credentials |
| GET | `/api/users/{id}` | 200 | 404 |
| PUT | `/api/users/{id}` | 200 | 404, 400 |
| DELETE | `/api/users/{id}` | 204 | 404 |

**Product** (`http://localhost:8082`)
| Method | Path | Success | Failure cases |
|---|---|---|---|
| GET | `/api/products` | 200 (list) | — |
| GET | `/api/products/{id}` | 200 | 404 |
| POST | `/api/products` | 201 | 400 |
| PUT | `/api/products/{id}` | 200 | 404, 400 |
| DELETE | `/api/products/{id}` | 204 | 404 |

**Cart** (`http://localhost:8083`)
| Method | Path | Success | Failure cases |
|---|---|---|---|
| GET | `/api/cart/{userId}` | 200 (auto-creates empty cart) | — |
| POST | `/api/cart/{userId}/items` | 201 (returns cart + `totalPrice`) | 400 |
| DELETE | `/api/cart/{userId}/items/{itemId}` | 204 | 404 |
| DELETE | `/api/cart/{userId}` | 204 | 404 |

Every service returns a **consistent error envelope**, which is what makes negative testing clean:
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "user not found with id: 999" }
```

---

## Part 1 — Run the backend

### Prerequisites
- Java 17+, Maven 3.9+ (Docker optional)

### Option A — run each service with Maven (fastest for dev)
```bash
cd ecommerce-microservices
mvn clean package -DskipTests

# In three terminals (or background them):
java -jar user-service/target/user-service.jar
java -jar product-service/target/product-service.jar
java -jar cart-service/target/cart-service.jar
```

### Option B — Docker Compose (one command, all three)
```bash
cd ecommerce-microservices
docker-compose up --build
```

### Verify they are up
```bash
curl http://localhost:8081/actuator/health   # {"status":"UP"}
curl http://localhost:8082/api/products        # seeded list
curl http://localhost:8083/api/cart/1          # empty cart
```
Each service also exposes an H2 console at `/h2-console` (JDBC URL e.g. `jdbc:h2:mem:usersdb`).

---

## Part 2 — Run the tests

With the three services running:
```bash
cd RestAssured_Framework_OnlineStoreAPITesting
mvn clean test
```
Outputs produced:
- `reports/ExtentReport.html` — interactive pass/fail dashboard
- `logs/test_logging.txt` — full request + response of **every** call (your first debugging stop)
- `allure-results/` — raw Allure data → `allure serve allure-results` for a rich report

Run a single class or method:
```bash
mvn test -Dtest=CartTests
mvn test -Dtest=UserTests#loginWrongPassword
```

**Current status: 29 tests, all passing** (user 9, product 8, cart 7, schema 3, plus data-driven rows).

---

## Part 3 — The framework, layer by layer (the senior-tester walkthrough)

The golden rule of a maintainable API framework: **a test method should read like a sentence,
and the plumbing should live somewhere else.** Here is where each responsibility lives and *why*.

### Payloads — file-based templates (this framework's choice)
Request bodies are **JSON template files** under `src/test/resources/payloads/`
(`registerUser.json`, `loginUser.json`, `createProduct.json`, `addCartItem.json`), each with
`{{placeholder}}` tokens:
```json
{ "username": "{{username}}", "password": "{{password}}", "email": "{{email}}", ... }
```
`payloads/PayloadManager.java` reads a template off the classpath and fills the tokens:
```java
Map<String,Object> data = PayloadManager.randomUserData();          // unique values
String body = PayloadManager.build("registerUser.json", data);      // -> ready JSON
```
**Why templates, not plain static JSON?** A hardcoded username hits `409 already taken` on the
second run. Filling `{{username}}` with a unique value each run keeps the suite repeatable. The
test owns the data map, so it can **reuse** those values later (register, then log in with the
exact same username/password rendered from `loginUser.json`).

**File-based vs POJO — the tradeoff a senior tester weighs:**
| | File-based templates (used here) | POJO + Jackson |
|---|---|---|
| Body reads like the real request | ✅ exactly | ❌ indirect |
| Non-Java testers can edit bodies | ✅ | ❌ |
| Compile-time safety on field names | ❌ (strings) | ✅ rename breaks the build |
| Malformed-JSON negative tests | ✅ trivial (edit the file) | ❌ hard (object is always valid) |

Neither is "correct" — this framework uses files because the bodies stay human-readable and
editable, which is what most manual-to-automation testers prefer.

### `routes/` — the URL map + request wrappers
- `Routes.java` holds every base URL and path **once**. When an endpoint moves, you edit one line.
- `UserEndpoints` / `ProductEndpoints` / `CartEndpoints` wrap `RestAssured.given()...` in named
  static methods (`createUser`, `getCart`, `addItem`). Tests call `CartEndpoints.addItem(userId, item)`
  and never touch the HTTP DSL directly. This is the single most important maintainability move
  in the whole framework.

### `payloads/PayloadManager.java` — the template renderer + data factory
Reads the JSON templates and uses **JavaFaker** to generate valid-but-random values, so every run
gets a fresh username/email. Fixture data that tests fight over is the #1 cause of flaky API
suites — randomized identities kill that class of flakiness.

### `utils/`
- `ConfigReader` — loads `Config.properties`, and a `-D` system property overrides any value
  (this is how you point at a hosted environment without touching code).
- `DataProviders` — TestNG `@DataProvider`s that read the JSON files in `testdata/` so you add a
  test case by editing data, not code (data-driven testing).
- `ExtentReporter` — a TestNG `ITestListener` that builds the Extent HTML report. `ThreadLocal`
  keeps per-test state correct under parallel execution.

### `testcases/`
- `BaseClass` — `@BeforeClass` attaches `RequestLoggingFilter` + `ResponseLoggingFilter` so every
  request/response is written to `logs/`. All test classes extend it.
- `UserTests`, `ProductTests`, `CartTests` — full lifecycle **plus** negative paths.
- `SchemaTests` — contract tests using `matchesJsonSchemaInClasspath(...)`.

### The REST Assured call, decoded
```java
given()                                  // start building a request
   .baseUri(Routes.USER_BASE)            // which service
   .header("Content-Type","application/json")
   .body(payload)                        // POJO -> JSON automatically
.when()
   .post(Routes.USER_REGISTER)           // the action
.then()
   .statusCode(201)                      // assert status
   .body("username", equalTo("jdoe"))    // assert a JSON field (Hamcrest matcher)
   .body("id", notNullValue());
```
`body("items[0].productId", equalTo(1))` uses **GPath** (JSON path) to reach into nested/array
responses — that is how `CartTests` asserts on items inside the cart.

### Positive vs negative — why both
A suite that only checks 200/201 gives false confidence. Notice every service test also asserts
the failure branch: 404 (missing id), 400 (bad body), 401 (bad credentials), 409 (duplicate).
**A senior tester spends more time on the error paths than the happy path**, because that is where
services actually break in production.

### Contract (schema) tests — the cheapest safety net
`SchemaTests` validates response *shape* against `*.json` schemas, independent of values. If a
developer renames `stockQuantity` or changes `price` from number to string, the schema test fails
even though the status is still 200. This catches accidental breaking changes early and for free.

---

## Part 4 — Deploy to the cloud, then test the live environment

### Option A — Render (free tier, Docker) — recommended
1. Push this repo to GitHub (see below).
2. Render → **New + → Blueprint** → select this repo. It reads `ecommerce-microservices/render.yaml`
   and creates three Docker web services.
3. You get three HTTPS URLs, e.g. `https://user-service-xxxx.onrender.com`.

Each service binds to the platform's injected `PORT` automatically (`server.port: ${PORT:8081}`),
so no extra config is needed.

### Option B — Railway
Create a service per folder, set the Dockerfile path, deploy. Railway also injects `PORT`, which
the same `${PORT:...}` handles.

### Option C — docker-compose (local or any VM)
```bash
cd ecommerce-microservices && docker-compose up --build
```

### Pointing the tests at a hosted environment
No code change — override the base URLs at run time:
```bash
mvn test \
  -Duser.baseUrl=https://user-service-xxxx.onrender.com \
  -Dproduct.baseUrl=https://product-service-xxxx.onrender.com \
  -Dcart.baseUrl=https://cart-service-xxxx.onrender.com
```
That indirection (`ConfigReader.get` reads `-D` first) is exactly why base URLs never belong in
test code.

> Free-tier note: Render/Railway free instances **sleep when idle** and cold-start in ~30–60s.
> The first request after a nap may time out. For CI, either hit a health endpoint to wake them
> first, or add a retry.

---

## Part 5 — Push to GitHub

The repo is set up push-ready (`.gitignore` excludes `target/`, `logs/`, `reports/`, `allure-results/`).
```bash
cd D:\RestAssured
git init
git add .
git commit -m "ShoppingCart microservices + REST Assured framework"
git branch -M main
git remote add origin https://github.com/ManjulaNenavath/ShoppingCartMicroService.git
git push -u origin main
```

---

## Part 6 — What to learn next (a senior tester's roadmap)

You now have the fundamentals wired end-to-end. Level up in this order:

1. **Request/Response Specifications** (`RequestSpecBuilder`) — factor out common headers/auth into
   a reusable spec instead of repeating `given()` setup.
2. **Real auth** — swap the demo token for a signed JWT, then practice sending
   `Authorization: Bearer <token>` and testing 401/403 boundaries.
3. **Serialization both ways** — deserialize responses straight into POJOs
   (`response.as(UserResponse.class)`) and assert on objects, not JSON paths.
4. **CI** — run `mvn test` in GitHub Actions on every push; publish the Allure report as an artifact.
5. **Contract testing at scale** — generate schemas from the live OpenAPI spec so they never drift.
6. **Non-functional** — response-time assertions (`time(lessThan(...))`), then load testing with
   Gatling/JMeter reusing these same endpoints.
7. **Test data lifecycle** — teardown created records in `@AfterClass` so runs stay independent
   even against a shared/hosted DB.

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `Connection refused` in tests | A service isn't running. Check all three health endpoints. |
| Product list test fails "empty" | Restart product-service; it re-seeds 3 products on boot. |
| 409 on a rerun | You hard-coded a username somewhere; use `Payload.newUser()`. |
| Allure report empty | Results are in `allure-results/`; run `allure serve allure-results`. |
| Port already in use | Another process holds 8081-8083; stop it or change the port in `application.yml`. |
