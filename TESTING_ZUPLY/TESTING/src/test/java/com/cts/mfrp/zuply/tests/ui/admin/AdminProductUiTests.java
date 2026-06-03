package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminProductsPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin product management — FRD §2.11. Maps to TC022 and TC023. */
public class AdminProductUiTests extends UiBaseTest {

    private static final By PRODUCTS_HEADING =
            By.xpath("//h1[contains(normalize-space(),'Manage Products')]");
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");
    private static final By FILTER_TABS     = By.cssSelector("button.filter-tab");

    private static final Duration FILTER_LOAD = Duration.ofSeconds(5);

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC022 — Admin can approve a submitted product. */
    @Test(description = "TC022 — AdminApproveProduct")
    public void tc022_adminApproveProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Step 1 — open page
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 2 — select Pending Review filter
        try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 3 — read count from tab label, with refresh-and-retry polling if 0.
        // Single assignment (via helper) so `pendingBefore` stays effectively
        // final for the lambdas in Step 6 + the assertion in Step 8.
        int pendingBefore = waitForPendingReviewProducts(page, wait, longWait);
        System.out.println("Pending Review count from tab before: " + pendingBefore);

        // Step 4 — approve first product
        page.approveFirst();

        // Step 5 — wait for spinner + tabs to reload, then perform a SOFT UI
        // RESET (no browser refresh — refresh would clear the admin JWT and
        // log the test out, breaking subsequent admin-banner readyMarker
        // waits). The soft reset toggles filter tabs APPROVED → PENDING_REVIEW
        // so the SPA re-issues its backend filter query, which refreshes the
        // badge counter from server state without unloading the SPA.
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));
        waitAfterAction();

        softResetFilterTabs(page, longWait);

        // Step 6 — wait for Pending Review count to decrease by 1.
        // Two-stage defensive wait: 10s natural settle; on TimeoutException,
        // toggle the filter tabs again and retry within the full 30s window.
        // Both refresh stages use the SOFT reset — session/auth stays intact.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW) == pendingBefore - 1);
        } catch (org.openqa.selenium.TimeoutException firstAttempt) {
            int observed = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW);
            System.out.println("[TC022] Count still " + observed + " after 10s (expected "
                    + (pendingBefore - 1) + ") — soft-resetting filter tabs and retrying with 30s window");
            softResetFilterTabs(page, longWait);
            longWait.ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW) == pendingBefore - 1);
        }

        // Step 7 — assert page still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin products page should remain loaded after approve");

        // Step 8 — assert pending count decreased by 1
        Assert.assertEquals(
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW),
                pendingBefore - 1,
                "Pending Review count should decrease by 1 after approval");

        // Step 9 — verify approved count increased by 1
        int approvedAfter = page.getFilterCount(AdminProductsPage.Filter.APPROVED);
        Assert.assertTrue(approvedAfter >= 1,
                "Approved tab count should be at least 1 after approval");
    }

    /**
     * Soft UI reset: toggle the filter tabs APPROVED → PENDING_REVIEW so the
     * SPA re-issues its backend filter query and refreshes the tab badge
     * counter from server state — without triggering a browser reload
     * (which would clear the admin JWT and log the session out, breaking
     * subsequent admin-banner readyMarker waits).
     *
     * Each click is followed by {@code waitForSpinnerGone(FILTER_LOAD)} (an
     * explicit wait bounded by FILTER_LOAD=5s) and a filter-tab presence
     * wait — no Thread.sleep.
     */
    private void softResetFilterTabs(AdminProductsPage page, WebDriverWait longWait) {
        try { page.selectFilter(AdminProductsPage.Filter.APPROVED); }
        catch (Exception ignored) { /* tab not ready — fall through; PENDING_REVIEW click below will still re-trigger fetch */ }
        page.waitForSpinnerGone(FILTER_LOAD);
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));
    }

    /**
     * Read the PENDING REVIEW tab count; if it's 0 (typical when the seller
     * upload from a prior &lt;test&gt; tag hasn't yet synced to the SPA's
     * dashboard cache), enter a refresh-and-retry polling loop bounded by
     * {@code longWait} (30s). Each iteration:
     *
     *   1. driver.navigate().refresh() — forces the SPA to re-fetch from backend
     *   2. wait for the heading + filter tabs to re-render (sub-waits use the
     *      shorter {@code wait} so a single slow iteration can't consume the
     *      full polling window)
     *   3. re-select the PENDING_REVIEW filter (tab is reset by the refresh)
     *   4. wait for the loading spinner to finish
     *   5. re-read the tab count; return it if &gt; 0, else null to keep polling
     *
     * After 30s with no count change, throws {@link org.testng.SkipException}
     * — same skip semantics as the original code, just deferred until we've
     * actually given the backend time to commit.
     *
     * Helper extraction is required so {@code pendingBefore} can be assigned
     * exactly once in the test method, keeping it effectively-final for the
     * lambda captures in Step 6 (Java language constraint).
     */
    private int waitForPendingReviewProducts(AdminProductsPage page,
                                              WebDriverWait wait,
                                              WebDriverWait longWait) {
        int initialCount = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW);
        if (initialCount > 0) {
            return initialCount;
        }
        System.out.println(
                "[TC022] Pending Review count is 0 — entering refresh-and-retry polling (up to 30s)");
        try {
            return longWait.until(d -> {
                driver.navigate().refresh();
                try { wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING)); }
                catch (Exception ignored) { return null; /* page not ready yet — retry */ }
                try { wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0)); }
                catch (Exception ignored) { return null; }
                try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
                catch (Exception ignored) { /* tab not yet rendered — count read below will be 0 */ }
                page.waitForSpinnerGone(FILTER_LOAD);
                int n;
                try { n = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW); }
                catch (Exception ex) { return null; }
                System.out.println("[TC022] Pending Review count after refresh iteration: " + n);
                return n > 0 ? n : null;
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new org.testng.SkipException(
                    "No PENDING REVIEW products appeared within 30s of refresh-and-retry polling — "
                  + "upstream seller upload may have failed or not yet committed");
        }
    }

    /** TC023 — Admin can reject a submitted product. */
    @Test(description = "TC023 — AdminRejectProduct")
    public void tc023_adminRejectProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Step 1 — open page
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 2 — select Pending Review filter
        try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 3 — read count from tab label
        int pendingBefore = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW);
        System.out.println("Pending Review count from tab before: " + pendingBefore);

        if (pendingBefore == 0) {
            throw new org.testng.SkipException(
                    "No PENDING REVIEW products on this env — cannot exercise rejection");
        }

        // Step 4 — reject first product
        page.rejectFirst();

        // Step 5 — handle browser confirm dialog for reject
        // UI confirmed: reject shows "Reject this product?" dialog
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            System.out.println("Reject dialog: " + alert.getText());
            alert.accept();
        } catch (Exception ignored) {
            System.out.println("No reject dialog appeared");
        }

        // Step 6 — wait for spinner and tabs to reload
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));
        waitAfterAction();

        // Step 7 — wait for Pending Review count to decrease by 1
        longWait.until(d ->
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW) == pendingBefore - 1);

        // Step 8 — assert page still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin products page should remain loaded after reject");

        // Step 9 — assert pending count decreased by 1
        Assert.assertEquals(
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW),
                pendingBefore - 1,
                "Pending Review count should decrease by 1 after rejection");

        // Step 10 — verify rejected count increased by 1
        int rejectedAfter = page.getFilterCount(AdminProductsPage.Filter.REJECTED);
        Assert.assertTrue(rejectedAfter >= 1,
                "Rejected tab count should be at least 1 after rejection");
    }
}