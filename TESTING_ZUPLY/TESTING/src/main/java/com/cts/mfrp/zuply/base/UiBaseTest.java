package com.cts.mfrp.zuply.base;

import com.cts.mfrp.zuply.constants.AppConstants;
import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.pages.RegisterPage;
import com.cts.mfrp.zuply.utils.DriverFactory;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import com.cts.mfrp.zuply.utils.UiTestDataAugmenter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shared base for all UI tests. Spins up a fresh visible Chrome by default
 * (toggle to background mode via {@code -Dheadless=true}) before each test
 * method and tears it down afterwards. Exposes auth shortcuts (admin/customer/seller).
 *
 * UI tests target https://zuply.netlify.app/. The SPA is Angular 18; Netlify
 * does NOT provide SPA fallback routing, so we always boot at "/" and let the
 * page object's {@code open()} use the Angular router. All credentials come
 * from environment / system properties so the file has no hard-coded secrets.
 */
public abstract class UiBaseTest {

    public static final String BASE_URL = "https://zuply.netlify.app";

    /**
     * Global explicit-wait window for UI tests. Bumped from 15s to 25s in response
     * to TimeoutException churn on SPA cold-starts. Override per-call when a
     * specific operation needs longer (e.g. AI generation).
     */
    public static final Duration DEFAULT_WAIT = Duration.ofSeconds(25);

    /**
     * Augment the UI_*Data.xlsx workbooks with any sheets new tests have started
     * relying on before TestNG even loads the first @BeforeClass. Idempotent —
     * sheets already present are left untouched. Failures here are swallowed
     * (logged to stderr) so a permissions issue on the test data dir doesn't
     * mask the actual test outcome; the per-test getRowByTestCaseId call will
     * raise a clear IllegalArgumentException if a sheet truly couldn't be added.
     */
    static {
        try { UiTestDataAugmenter.run(); }
        catch (Exception e) {
            System.err.println("[UiBaseTest] UiTestDataAugmenter failed: " + e.getMessage());
        }
    }

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** Pre-seeded admin login on the SPA's backend. Override via -Dadmin.email / -Dadmin.password. */
    protected String adminEmail()    { return System.getProperty("admin.email",    "admin@zuply.in"); }
    protected String adminPassword() { return System.getProperty("admin.password", "Admin@123"); }

    /**
     * Shared password used when auto-registering customers/sellers for UI runs.
     * Sourced from {@code UI_AuthData.xlsx → Defaults → DefaultPassword}; falls
     * back to the historical literal {@code "Test@1234"} when the workbook or
     * sheet is unavailable (so the suite is still runnable on a checkout that
     * hasn't yet run {@code UiTestDataAugmenter}). Override at runtime with
     * {@code -Ddefault.password=...}.
     */
    protected String defaultPassword() { return defaultsLookup("DefaultPassword", "default.password", "Test@1234"); }

    /** Shared phone used at auto-registration time. See {@link #defaultPassword()}. */
    protected String defaultPhone() { return defaultsLookup("DefaultPhone", "default.phone", "9876543210"); }

    private static volatile Map<String, String> DEFAULTS_CACHE;

    private static synchronized Map<String, String> loadDefaults() {
        if (DEFAULTS_CACHE != null) return DEFAULTS_CACHE;
        Map<String, String> out = new HashMap<>();
        try {
            for (Map<String, String> row : ExcelUtils.getTestDataAsMaps(
                    AppConstants.TESTDATA_DIR + "UI_AuthData.xlsx", "Defaults")) {
                String k = row.getOrDefault("Key", "");
                if (!k.isEmpty()) out.put(k, row.getOrDefault("Value", ""));
            }
        } catch (Exception ignored) {
            // Sheet missing — keep map empty so callers fall back to their literal default.
        }
        DEFAULTS_CACHE = out;
        return out;
    }

    private static String defaultsLookup(String key, String sysPropOverride, String fallback) {
        String prop = System.getProperty(sysPropOverride);
        if (prop != null && !prop.isBlank()) return prop;
        String fromSheet = loadDefaults().get(key);
        return (fromSheet != null && !fromSheet.isBlank()) ? fromSheet : fallback;
    }

    @BeforeClass(alwaysRun = true)
    @Parameters({"headless"})
    public void launchBrowser(@Optional("false") String headless) {
        driver = DriverFactory.create(Boolean.parseBoolean(headless));
        DriverFactory.setDriver(driver);
        wait = new WebDriverWait(driver, DEFAULT_WAIT);
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            DriverFactory.setDriver(null);
            driver = null;
        }
    }

    /**
     * Clear cookies + localStorage + sessionStorage to drop the current login.
     * Use in tests that need to switch from logged-in state to anonymous (e.g.
     * a "must login to view wishlist" scenario).
     */
    protected void clearSession() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
    }

    /* ------------------------------------------------------------------ */
    /* Shared helpers                                                      */
    /* ------------------------------------------------------------------ */

    /** Returns a random 8-char alphanumeric suffix for unique emails. */
    protected static String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * Dynamic-email generator using the requested {@code System.currentTimeMillis()}
     * pattern, with a small UUID suffix appended to defeat the rare same-millisecond
     * collision when two tests start in parallel. Use whenever a script needs to
     * synthesize a fresh email (most common case: an Excel cell contains a placeholder
     * like {@code <RANDOM>}, the literal {@code "random"}, or is blank).
     */
    protected static String generateDynamicEmail(String prefix) {
        String safe = (prefix == null || prefix.isBlank()) ? "user"
                : prefix.toLowerCase().replaceAll("\\s+", "");
        return "ui." + safe + "." + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 4) + "@zuply.in";
    }

    /**
     * Returns the email value to actually submit to the form. Mirrors the
     * "hybrid data strategy" — Excel holds static values; the script generates
     * uniqueness. A cell is treated as a placeholder (and replaced with a
     * freshly generated unique email) when it is:
     *
     *   • blank / whitespace-only
     *   • the token {@code <RANDOM>} or {@code ${rand}}
     *   • the literal string {@code "random"} (case-insensitive) — accepted
     *     defensively in case a data row was authored with the bare word
     *
     * Otherwise the cell value is returned verbatim (real test email).
     */
    protected static String resolveEmailCell(String cellValue, String namePrefix) {
        if (cellValue == null) return generateDynamicEmail(namePrefix);
        String v = cellValue.trim();
        if (v.isEmpty()
                || v.equalsIgnoreCase("random")
                || v.equalsIgnoreCase("<RANDOM>")
                || v.equalsIgnoreCase("${rand}")) {
            return generateDynamicEmail(namePrefix);
        }
        return v;
    }

    /** Register a fresh user via the SPA's signup form; returns the email used. */
    protected String registerNewCustomer(String namePrefix) {
        String email = "ui." + namePrefix.toLowerCase().replaceAll("\\s+","") + "." + randomSuffix() + "@zuply.in";
        new RegisterPage(driver).open();
        new RegisterPage(driver).registerAs(namePrefix, email, defaultPhone(), defaultPassword(),
                RegisterPage.Role.CUSTOMER);
        // Wait until the router leaves /register, then allow backend to commit the account
        try { wait.until(d -> !d.getCurrentUrl().contains("/register")); }
        catch (Exception ignored) {}
        waitAfterAction();
        return email;
    }

    protected String registerNewSeller(String namePrefix) {
        String email = "ui." + namePrefix.toLowerCase().replaceAll("\\s+","") + "." + randomSuffix() + "@zuply.in";
        new RegisterPage(driver).open();
        new RegisterPage(driver).registerAs(namePrefix, email, defaultPhone(), defaultPassword(),
                RegisterPage.Role.SELLER);
        try { wait.until(d -> !d.getCurrentUrl().contains("/register")); }
        catch (Exception ignored) {}
        waitAfterAction();
        return email;
    }

    /**
     * Drive through the login page. Retries up to 3 times with a short gap
     * between attempts to handle slow backend registration on cold starts.
     *
     * On failure, captures live page state (input.value, URL, button-enabled,
     * visible error banner) and includes it in the thrown RuntimeException so
     * the next recurrence tells us *why* login failed — usually one of:
     *   • Selenium sendKeys race left a field blank → fix at {@code BasePage.type}
     *     value-verification path (already in place).
     *   • Registration succeeded client-side but backend never created the account
     *     → button stays disabled because async email-exists validator fails.
     *     Symptom: both fields populated, button.disabled==true, no banner.
     *   • Account exists but credentials wrong → button enables and click fires,
     *     but URL stays /login + banner shows "Invalid credentials". Different
     *     code path (we don't hit this branch).
     */
    protected void loginViaUi(String email, String password) {
        LoginPage login = new LoginPage(driver);
        Exception lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                login.open();
                login.loginAs(email, password);
                return;
            } catch (Exception e) {
                lastError = e;
                if (attempt < 3) waitAfterAction();
            }
        }
        throw new RuntimeException(
                "Login failed after 3 attempts for " + email + ". " + captureLoginDiagnostics(),
                lastError);
    }

    /**
     * Snapshot of the SPA's login form at the moment login gave up. Read-only —
     * never throws. The captured fields tell us the failure mode:
     *
     *   emailValue / passwordValue blank  → sendKeys race; type() value-verification
     *                                       should self-heal — investigate that path
     *   button.disabled=true, both fields → backend rejected registration OR async
     *     populated, no banner               email-exists validator is failing
     *   button.disabled=true, banner shown → form-level error visible — read .banner
     *   button.disabled=false              → click never fired, or URL didn't update
     */
    private String captureLoginDiagnostics() {
        StringBuilder sb = new StringBuilder("Diagnostics: ");
        try { sb.append("url=").append(driver.getCurrentUrl()).append("; "); }
        catch (Exception e) { sb.append("url=<unreadable>; "); }

        sb.append("email.value=").append(quote(readInputValue("input[type='email'].input"))).append("; ");
        sb.append("password.value.len=").append(readInputValueLength("input[type='password'].input")).append("; ");
        sb.append("login-btn.disabled=").append(readButtonDisabled("button.login-btn")).append("; ");
        sb.append("banner=").append(quote(readFirstBannerText())).append('.');
        return sb.toString();
    }

    private String readInputValue(String cssSelector) {
        try {
            java.util.List<WebElement> els = driver.findElements(By.cssSelector(cssSelector));
            if (els.isEmpty()) return "<no-element>";
            String v = els.get(0).getDomProperty("value");
            return v == null ? "" : v;
        } catch (Exception e) { return "<read-error>"; }
    }

    private int readInputValueLength(String cssSelector) {
        String v = readInputValue(cssSelector);
        return v == null || v.startsWith("<") ? -1 : v.length();
    }

    private String readButtonDisabled(String cssSelector) {
        try {
            java.util.List<WebElement> els = driver.findElements(By.cssSelector(cssSelector));
            if (els.isEmpty()) return "<no-button>";
            String d = els.get(0).getDomProperty("disabled");
            return d == null ? "false" : d;
        } catch (Exception e) { return "<read-error>"; }
    }

    private String readFirstBannerText() {
        try {
            java.util.List<WebElement> els = driver.findElements(By.cssSelector(
                    "[role='alert'], .toast, .alert, .alert-danger, .notification, " +
                    "[class*='toast'], [class*='snack'], [class*='banner'][class*='error']"));
            if (els.isEmpty()) return "<none>";
            String t = els.get(0).getText();
            return t == null ? "" : t.trim();
        } catch (Exception e) { return "<read-error>"; }
    }

    private static String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\n", "\\n") + "\"";
    }

    protected void loginAsAdmin() {
        loginViaUi(adminEmail(), adminPassword());
    }

    /**
     * Login wrapper that tolerates SPA quirks after self-registration:
     *   - The register flow sometimes auto-logs the user in — visiting /login
     *     then submitting will land us back on /login while the SPA redirects,
     *     and {@code LoginPage.loginAs()} throws TimeoutException waiting for
     *     the URL to change.
     *   - A PENDING (unapproved) seller may briefly stay on /login while the
     *     SPA processes the response.
     *
     * Strategy: if we are already on a {@code /seller/*} or {@code /admin/*}
     * route, skip login entirely. Otherwise call {@link #loginViaUi} but swallow
     * a TimeoutException — the next page's {@code open()} will surface the real
     * failure via its own readyMarker wait.
     */
    protected void ensureLoggedIn(String email, String password) {
        String url = driver.getCurrentUrl();
        if (url != null && (url.contains("/seller/") || url.contains("/admin/"))) {
            return;
        }
        try {
            loginViaUi(email, password);
        } catch (TimeoutException ignored) {
            // URL didn't leave /login within the LoginPage's wait window.
            // Continue — downstream page.open() calls will verify state.
        }
        // Best-effort: wait briefly for navigation to settle.
        try {
            new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(d -> {
                        String u = d.getCurrentUrl();
                        return u != null && !u.contains("/login");
                    });
        } catch (TimeoutException ignored) { /* still on /login — let test decide */ }
    }

    /**
     * Waits for a toast/alert to appear after a button-click action, then returns.
     * Falls through silently if no toast appears within 3 s (some actions complete
     * without a visual notification).
     */
    protected void waitAfterAction() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                            "[role='alert'], .toast, .notification, [class*='toast'], [class*='snack']")));
        } catch (Exception ignored) {}
    }

    /**
     * Navigates within the SPA via the Angular router (pushState + popstate)
     * without triggering a full page reload. Used to test route guards for
     * pages that are not supposed to be accessible without authentication.
     * Alias of {@link #navigateRoute(String)} kept for tests authored on the
     * {@code likhitha} branch.
     */
    protected void navigateToRoute(String route) {
        navigateRoute(route);
    }

    /** Best-effort JS click — bypasses overlay-intercepted clicks (e.g. chat FAB). */
    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    protected void jsClick(By by) { jsClick(driver.findElement(by)); }

    /**
     * Navigate the SPA to {@code route} via {@code history.pushState} + a synthetic
     * popstate event. Used by tests that intentionally attempt routes the current
     * user isn't authorized for — these tests can't instantiate the target page
     * object because its {@link com.cts.mfrp.zuply.pages.BasePage#open()} would
     * wait on a readyMarker that will never appear under unauthorized access.
     */
    protected void navigateRoute(String route) {
        ((JavascriptExecutor) driver).executeScript(
                "const p = arguments[0];" +
                "const a = document.querySelector('a[href=\"'+p+'\"], a[routerlink=\"'+p+'\"]');" +
                "if (a) a.click(); else { history.pushState({}, '', p); window.dispatchEvent(new PopStateEvent('popstate')); }",
                route);
    }
}
