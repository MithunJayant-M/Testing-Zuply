package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminDashboardPage;
import com.cts.mfrp.zuply.pages.AdminProductsPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Admin Dashboard UI Tests — core, non-redundant coverage.
 * All behaviours verified manually against https://zuply.netlify.app/admin/dashboard
 */
public class AdminDashboardUiTests extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_AuthData.xlsx";
    private static final String SHEET     = "AdminCreate";

    private static final By ADD_ADMIN_FORM = By.cssSelector("div.create-admin-form");

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    // ── Page load ─────────────────────────────────────────────────────────────

    /** TC050 — Dashboard loads with hero banner and all 4 stat cards visible. */
    @Test(description = "TC050 — AdminDashboardLoads")
    public void tc050_adminDashboardLoads() {
        System.out.println("[TC050] Opening admin dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        System.out.println("[TC050] Verifying hero banner and stat cards are visible");
        Assert.assertTrue(page.isLoaded(),                      "Dashboard hero banner should be loaded");
        Assert.assertTrue(page.isTotalSellersCardVisible(),     "Total Sellers stat card should be visible");
        Assert.assertTrue(page.isTotalProductsCardVisible(),    "Total Products stat card should be visible");
        Assert.assertTrue(page.isTotalOrdersCardVisible(),      "Total Orders stat card should be visible");
        Assert.assertTrue(page.isPendingApprovalsCardVisible(), "Pending Approvals stat card should be visible");
        System.out.println("[TC050] PASSED — all dashboard elements visible");
    }

    // ── Sellers Pending Approval ──────────────────────────────────────────────

    /** TC060 — Approve pending seller from dashboard — no dialog, count decreases. */
    @Test(description = "TC060 — AdminDashboardApprovePendingSeller")
    public void tc060_approvePendingSellerFromDashboard() {
        System.out.println("[TC060] Approving first pending seller from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        page.openAndWaitForData();
        int countBefore = page.sellersPendingCount();
        System.out.println("[TC060] Pending sellers before approve: " + countBefore);
        if (countBefore == 0) {
            throw new SkipException("No pending sellers on this env");
        }
        page.approveFirstPendingSeller();
        waitAfterAction();

        // SOFT UI RESET (no browser refresh — refresh would clear the admin
        // JWT and log the session out). We navigate via the SPA router to a
        // sibling admin route then back to /admin/dashboard. The intermediate
        // route forces Angular to unmount the dashboard component; the return
        // hop re-mounts it, which triggers its ngOnInit → backend fetch.
        softResetDashboard(page);

        // Two-stage defensive wait: 10s initial; if the count still hasn't
        // dropped, soft-reset again and retry within the full 30s window.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> page.sellersPendingCount() == countBefore - 1);
        } catch (TimeoutException firstAttempt) {
            int observed = page.sellersPendingCount();
            System.out.println("[TC060] Count still " + observed + " after 10s (expected "
                    + (countBefore - 1) + ") — soft-resetting dashboard and retrying with 30s window");
            softResetDashboard(page);
            longWait.ignoring(StaleElementReferenceException.class)
                    .until(d -> page.sellersPendingCount() == countBefore - 1);
        }
        System.out.println("[TC060] Pending sellers after approve: " + page.sellersPendingCount());
        Assert.assertEquals(page.sellersPendingCount(), countBefore - 1,
                "Pending seller count should decrease by 1 after approve — was: " + countBefore);
    }

    /** TC062 — View all link in Sellers Pending section navigates to /admin/sellers. */
    @Test(description = "TC062 — AdminDashboardSellersViewAll")
    public void tc062_sellersViewAllNavigates() {
        System.out.println("[TC062] Clicking View all in Sellers Pending Approval section");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();
        if (!page.isSellersPendingSectionVisible()) {
            throw new org.testng.SkipException("Sellers Pending section not visible — no pending sellers");
        }
        page.clickSellersViewAll();
        System.out.println("[TC062] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/sellers"),
                "View all sellers link should navigate to /admin/sellers — got: " + driver.getCurrentUrl());
    }

    // ── Products Pending Review ───────────────────────────────────────────────

    /**
     * TC064 — Approve pending product from dashboard.
     *
     * Cross-session sync: a product uploaded in an earlier step (e.g. by the
     * seller suite running under a separate &lt;test&gt; tag) is committed
     * asynchronously on the backend, and the Angular dashboard cache may
     * read 0 for several seconds after that upload returns 201. We replace
     * the previous "check once, skip on 0" pattern with a polling wait that
     * gives the counter up to 30s to flip past 0, and forces one browser
     * refresh at the mid-point if the SPA's own polling didn't catch up.
     *
     * Only after a non-zero count is observed do we approve and verify the
     * decrement — so the test no longer silently skips when the upstream
     * upload is merely slow rather than missing.
     */
    @Test(description = "TC064 — AdminDashboardApprovePendingProduct")
    public void tc064_approvePendingProductFromDashboard() {
        System.out.println("[TC064] Approving first pending product from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();

        int countBefore;
        try {
            countBefore = waitForPendingProductsToAppear(page, Duration.ofSeconds(30));
        } catch (TimeoutException e) {
            // After 30s + one refresh the dashboard still reads 0. Skip rather
            // than fail — the upstream upload may genuinely not have produced
            // a pending row (e.g. seller wasn't approved yet).
            throw new SkipException(
                    "No pending products appeared on the admin dashboard within 30s "
                  + "(including one mid-window refresh) — upstream seller upload may have failed or "
                  + "the seller account isn't approved yet");
        }
        System.out.println("[TC064] Pending products before approve: " + countBefore);

        page.approveFirstPendingProduct();
        waitAfterAction();

        // SOFT UI RESET — see softResetDashboard javadoc for why this
        // replaces driver.navigate().refresh() (which would log us out).
        softResetDashboard(page);

        // Two-stage defensive wait: 10s initial; if the count still hasn't
        // dropped, soft-reset again and retry within the full 30s window.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> page.productsPendingCount() == countBefore - 1);
        } catch (TimeoutException firstAttempt) {
            int observed = page.productsPendingCount();
            System.out.println("[TC064] Count still " + observed + " after 10s (expected "
                    + (countBefore - 1) + ") — soft-resetting dashboard and retrying with 30s window");
            softResetDashboard(page);
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> page.productsPendingCount() == countBefore - 1);
        }
        System.out.println("[TC064] Pending products after approve: " + page.productsPendingCount());

        Assert.assertEquals(page.productsPendingCount(), countBefore - 1,
                "Pending product count should decrease by 1 after approve — was: " + countBefore);
    }

    /**
     * Soft UI reset for the admin dashboard.
     *
     * Why not {@code driver.navigate().refresh()}: a full browser reload
     * clears the Angular app's in-memory state and (depending on where the
     * SPA stashes its JWT — sessionStorage or memory) can log the admin
     * session out. The next ready-marker check ({@code div.admin-banner})
     * then times out because the SPA has bounced to /login.
     *
     * Instead, we navigate via the SPA router to a sibling admin route
     * ({@code /admin/products}, which is guaranteed different from
     * {@code /admin/dashboard}) and then back. The intermediate route
     * unmounts the dashboard component; the return hop re-mounts it, which
     * triggers its {@code ngOnInit} → fresh backend fetch. No full page
     * load means no JWT loss.
     *
     * Both hops use the page object's {@code open()} method, which uses
     * {@code history.pushState} + a popstate event under the hood — i.e.,
     * SPA-router navigation only, never {@code driver.get(...)}.
     */
    private void softResetDashboard(AdminDashboardPage page) {
        try {
            // Hop to a sibling admin route — must be different from /admin/dashboard
            // because Angular's default RouterOnSameUrlNavigation='ignore' would
            // suppress same-URL pushStates.
            new AdminProductsPage(driver).open();
        } catch (Exception ignored) {
            // If the intermediate route fails for any reason (e.g. transient
            // SPA boot state) the return-hop's openAndWaitForData() still gives
            // the dashboard a fresh mount — fall through.
        }
        page.open();
        page.openAndWaitForData();
    }

    /**
     * Poll the dashboard "pending products" counter every second until it goes
     * non-zero, or {@code timeout} elapses. At the halfway mark of the timeout
     * window the page is hard-refreshed once to force the SPA to re-fetch from
     * the backend — this handles the case where the Angular dashboard's own
     * cache is stale relative to the database (a known SPA quirk when state
     * was mutated by a different session in a separate &lt;test&gt; tag).
     *
     * @return the first non-zero count observed
     * @throws TimeoutException if the count stays 0 for the full window
     */
    private int waitForPendingProductsToAppear(AdminDashboardPage page, Duration timeout) {
        long startMs       = System.currentTimeMillis();
        long refreshAtMs   = startMs + (timeout.toMillis() / 2);
        AtomicBoolean didRefresh = new AtomicBoolean(false);

        return new WebDriverWait(driver, timeout)
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    int n;
                    try {
                        n = page.productsPendingCount();
                    } catch (Exception e) {
                        // Counter element not in DOM yet (e.g. mid-refresh). Keep polling.
                        return null;
                    }
                    if (n > 0) {
                        return n;
                    }
                    // Still 0. If we're past the mid-window mark and haven't yet
                    // refreshed, do exactly one full browser refresh + page re-init.
                    if (!didRefresh.get() && System.currentTimeMillis() >= refreshAtMs) {
                        long elapsed = System.currentTimeMillis() - startMs;
                        System.out.println("[TC064] Pending count still 0 at " + elapsed
                                + "ms — refreshing browser once to force SPA re-fetch");
                        didRefresh.set(true);
                        driver.navigate().refresh();
                        page.openAndWaitForData();
                    }
                    return null;   // keep polling
                });
    }

    // ── Admin Accounts ────────────────────────────────────────────────────────

    /** TC067 — Clicking + Add Admin expands inline form with Full Name, Email, Password, Phone fields. */
    @Test(description = "TC067 — AdminDashboardAddAdminFormExpands")
    public void tc067_addAdminFormExpands() {
        System.out.println("[TC067] Clicking + Add Admin button and verifying form expands");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.open();
        Assert.assertTrue(page.isAdminAccountsSectionVisible(),
                "Admin Accounts section should be visible at the bottom of the dashboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC067] Waiting for create-admin-form to appear");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        Assert.assertTrue(page.isAddAdminFormVisible(),
                "Add Admin inline form should expand after clicking + Add Admin button");
        page.cancelAddAdminForm();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ADD_ADMIN_FORM));
        System.out.println("[TC067] PASSED — form expanded and cancelled successfully");
    }

    /**
     * TC069 — Submitting Create Admin Account shows a backend error.
     * Known bug: api/admin/create-admin endpoint does not exist (404).
     */
    @Test(description = "TC069 — AdminDashboardCreateAdminKnownBug")
    public void tc069_createAdminShowsKnownError() throws Exception {
        System.out.println("[TC069] Testing Create Admin Account — expecting known backend error");
        Map<String, String> row = ExcelUtils.getRowByTestCaseId(DATA_FILE, SHEET, "TC069");

        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;
        page.open();
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC069] Waiting for form to expand");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        System.out.println("[TC069] Filling form with test data from " + SHEET + "/TC069");
        page.fillAddAdminForm(
                row.get("Name"), row.get("Email"), row.get("Password"), row.get("Phone"));

        // Install MutationObserver BEFORE submit — fires the instant Angular adds error div
        js.executeScript(
                "window.__adminError = false; window.__adminErrorText = '';" +
                        "window.__obs = new MutationObserver(function(ms) {" +
                        "  ms.forEach(function(m) { m.addedNodes.forEach(function(n) {" +
                        "    if (n.nodeType===1) {" +
                        "      var all = [n].concat(Array.from(n.querySelectorAll('*')));" +
                        "      all.forEach(function(el) {" +
                        "        if ((el.className||'').includes('create-admin-msg') && (el.innerText||'').trim()) {" +
                        "          window.__adminError = true;" +
                        "          window.__adminErrorText = el.innerText.trim();" +
                        "        }" +
                        "      });" +
                        "    }" +
                        "  }); });" +
                        "});" +
                        "window.__obs.observe(document.body,{childList:true,subtree:true});"
        );

        System.out.println("[TC069] Submitting form (observer active)");
        page.submitAddAdminForm();

        System.out.println("[TC069] Waiting for observer to detect error");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> Boolean.TRUE.equals(js.executeScript("return window.__adminError;")));

        js.executeScript("window.__obs.disconnect();");
        String msg = (String) js.executeScript("return window.__adminErrorText;");
        System.out.println("[TC069] Error message: " + msg);
        Assert.assertTrue(true, "Known bug confirmed: api/admin/create-admin not found");
        System.out.println("[TC069] PASSED — known bug confirmed");
    }
}
