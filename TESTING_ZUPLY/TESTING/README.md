# Zuply API Test Automation

REST Assured + TestNG + ExtentReports framework covering all 172 test cases across the 14 modules of the Zuply API.

## Project layout

admin login: admin@zuply.in , Admin@123
```
src/
├── main/java/com/cts/mfrp/Zuply/
│   ├── auth/          AuthManager - logs in once per role, caches JWT
│   ├── base/          BaseTest - RestAssured init, ExtentReports init
│   ├── clients/       One client class per resource (AuthClient, ProductClient, ...)
│   ├── constants/     AppConstants, Endpoints
│   └── Utils/         ConfigReader, ExcelUtils, TestDataHelper, TestDataGenerator,
│                      ExtentManager, ExtentReportListener, RetryAnalyzer, RequestBuilder
└── test/
    ├── java/com/cts/mfrp/Zuply/tests/   14 test classes, one per module
    └── resources/
        ├── config.properties            base.url, credentials, timeouts
        ├── config-staging.properties    staging overrides
        ├── testdata/*.xlsx              data-driven test rows (generated)
        └── suites/
            ├── testng.xml               sequential, one <test> per module
            └── parallel-testng.xml      parallel-classes, thread-count=4
```

## First-time setup

1. **Resolve dependencies** — REST Assured, ExtentReports, Jackson, Hamcrest are in `pom.xml`:
   ```
   mvn -q dependency:resolve
   ```

2. **Generate Excel test data** (one-time; safe to re-run):
   ```
   mvn -q compile exec:java -Dexec.mainClass="com.cts.mfrp.zuply.utils.TestDataGenerator"
   ```
   or just right-click `TestDataGenerator.main()` in your IDE. Files land in `src/test/resources/testdata/`.

## Running the tests

```
# Default suite (env=prod, base.url from config.properties)
mvn test

# Parallel execution
mvn test -DsuiteXmlFile=src/test/resources/suites/parallel-testng.xml

# Switch environment
mvn test -Denv=staging

# Override any property at the CLI
mvn test -Dbase.url=http://localhost:8080
```

ExtentReport HTML lands at `test-output/reports/ExtentReport.html`.

## Test data driving model

Each `*Tests.java` uses `@DataProvider` methods that read from the corresponding sheet in an `.xlsx` file via `TestDataHelper.read("AuthData.xlsx", "Login")`. The helper substitutes `${rand}` tokens with a random suffix per row so each run creates fresh emails / shop names without collisions.

Add new rows directly in the Excel file or extend `TestDataGenerator` to regenerate it.

## Authentication

`AuthManager` lazily fetches and caches a JWT for each role (`buyer`, `seller`, `admin`) on first use. Inside any test class extending `BaseTest`, just call `buyerToken()`, `sellerToken()`, `adminToken()`.

For negative-auth flows, call `AuthManager.login(email, password)` directly — it does not cache.

## Test counts (per spec)

| Module             | Tests |
|--------------------|------:|
| Authentication     | 10    |
| Users              | 8     |
| Products           | 20    |
| Reviews            | 8     |
| Categories         | 2     |
| Upload             | 6     |
| AI Listing         | 18    |
| Cart               | 14    |
| Wishlist           | 8     |
| Orders             | 10    |
| Payment            | 10    |
| Seller             | 18    |
| Admin              | 30    |
| Error Handling     | 10    |
| **Total**          | **172** |

A small number of tests assert on multi-status outcomes (e.g., `200 || 404`) where the row's expected outcome depends on environment-specific seeded data (e.g., whether order id 1 exists for the buyer in this DB). Tighten these per env once the seed data is locked.
