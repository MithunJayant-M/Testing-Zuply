package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Admin landing page at {@code /admin/dashboard}.
 *
 * Only the locators/actions exercised by the active test suite are exposed.
 * Helpers covering removed dashboard tests (nav links, shortcut cards, stat-value
 * readers, reject buttons, products-pending-visibility) were pruned to keep the
 * page surface aligned with the test surface.
 */
public class AdminDashboardPage extends BasePage {

    // ── Ready marker ──────────────────────────────────────────────────────────
    private static final By HERO_HEADING = By.cssSelector("div.admin-banner");

    // ── Stat cards (visibility checks only) ───────────────────────────────────
    private static final By CARD_TOTAL_SELLERS     = By.cssSelector("div[title='View approved sellers']");
    private static final By CARD_TOTAL_PRODUCTS    = By.cssSelector("div[title='View approved products']");
    private static final By CARD_TOTAL_ORDERS      = By.cssSelector("div[title='View all orders']");
    private static final By CARD_PENDING_APPROVALS = By.cssSelector("div[title='View pending approvals']");

    // ── Sellers Pending Approval section ──────────────────────────────────────
    private static final By SELLERS_PENDING_SECTION = By.cssSelector("div#pending-section");
    private static final By SELLERS_PENDING_BADGE   = By.cssSelector("div#pending-section div.section-title span.count-badge");
    private static final By SELLERS_PENDING_APPROVE = By.cssSelector("div#pending-section div.approval-card button.btn-primary");
    private static final By SELLERS_VIEW_ALL        = By.cssSelector("div#pending-section a.section-link");

    // ── Products Pending Review section ───────────────────────────────────────
    private static final By PRODUCTS_PENDING_BADGE   = By.cssSelector("div#pending-products-section div.section-title span.count-badge");
    private static final By PRODUCTS_PENDING_APPROVE = By.cssSelector("div#pending-products-section div.approval-card button.btn-primary");

    // ── Admin Accounts section ────────────────────────────────────────────────
    private static final By ADMIN_ACCOUNTS_SECTION  = By.cssSelector("div.section.create-admin-section");
    private static final By ADD_ADMIN_FORM          = By.cssSelector("div.create-admin-form");
    private static final By ADD_ADMIN_FORM_NAME     = By.cssSelector("input[placeholder='Admin name']");
    private static final By ADD_ADMIN_FORM_EMAIL    = By.cssSelector("input[placeholder='admin@example.com']");
    private static final By ADD_ADMIN_FORM_PASSWORD = By.cssSelector("input[placeholder='Strong password']");
    private static final By ADD_ADMIN_FORM_PHONE    = By.cssSelector("input[placeholder='Phone number'][type='tel']");
    private static final By ADD_ADMIN_CANCEL        = By.cssSelector("div.section.create-admin-section div.section-header button:not(.active)");

    public AdminDashboardPage(WebDriver driver) { super(driver); }

    @Override public String route()       { return "/admin/dashboard"; }
    @Override protected By readyMarker()  { return HERO_HEADING; }

    /**
     * Opens dashboard and waits for pending badges to load real data.
     * Badge renders with "0" initially then updates after API call — this
     * method waits for the badge to show a non-zero value before returning.
     * Use instead of open() for tests that check pending seller/product counts.
     */
    public void openAndWaitForData() {
        open();
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(SELLERS_PENDING_SECTION));
        } catch (Exception ignored) {}
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> {
                        try {
                            String t = d.findElement(SELLERS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
        } catch (Exception ignored) {}
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> {
                        try {
                            String t = d.findElement(PRODUCTS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
        } catch (Exception ignored) {}
    }

    // ── Stat card visibility ──────────────────────────────────────────────────
    public boolean isTotalSellersCardVisible()     { return isVisible(CARD_TOTAL_SELLERS); }
    public boolean isTotalProductsCardVisible()    { return isVisible(CARD_TOTAL_PRODUCTS); }
    public boolean isTotalOrdersCardVisible()      { return isVisible(CARD_TOTAL_ORDERS); }
    public boolean isPendingApprovalsCardVisible() { return isVisible(CARD_PENDING_APPROVALS); }

    // ── Sellers Pending Approval ──────────────────────────────────────────────
    public boolean isSellersPendingSectionVisible() { return isVisible(SELLERS_PENDING_SECTION); }

    /**
     * Reads count from span.count-badge. Badge renders with 0 first then updates —
     * wait for non-zero or stable value.
     */
    public int sellersPendingCount() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(SELLERS_PENDING_BADGE));
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> {
                        try {
                            String t = d.findElement(SELLERS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
            return Integer.parseInt(driver.findElement(SELLERS_PENDING_BADGE).getText().trim());
        } catch (Exception e) { return 0; }
    }

    /** UI confirmed: Approve on dashboard has NO browser dialog. */
    public void approveFirstPendingSeller() {
        scrollAndClick(firstOf(SELLERS_PENDING_APPROVE, "Seller Approve"));
    }

    public void clickSellersViewAll() { click(SELLERS_VIEW_ALL); }

    // ── Products Pending Review ───────────────────────────────────────────────

    /**
     * Reads count from span.count-badge. Badge renders with 0 first then updates —
     * wait for non-zero or stable value.
     */
    public int productsPendingCount() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(PRODUCTS_PENDING_BADGE));
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> {
                        try {
                            String t = d.findElement(PRODUCTS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
            return Integer.parseInt(driver.findElement(PRODUCTS_PENDING_BADGE).getText().trim());
        } catch (Exception e) { return 0; }
    }

    /** UI confirmed: Approve on dashboard has NO browser dialog. */
    public void approveFirstPendingProduct() {
        scrollAndClick(firstOf(PRODUCTS_PENDING_APPROVE, "Product Approve"));
    }

    // ── Admin Accounts ────────────────────────────────────────────────────────
    public boolean isAdminAccountsSectionVisible() { return isVisible(ADMIN_ACCOUNTS_SECTION); }
    public boolean isAddAdminFormVisible()         { return isVisible(ADD_ADMIN_FORM); }

    public void clickAddAdmin() {
        // FIX: use pure JS click instead of scroll + native click. In headless mode
        // window.scrollTo does not reliably trigger Angular to render the Admin
        // Accounts section — JS click works regardless of viewport position in
        // both headless and non-headless mode.
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector(" +
                        "'div.section.create-admin-section div.section-header button.btn-primary'" +
                        ").click();"
        );
    }

    public void fillAddAdminForm(String name, String email, String password, String phone) {
        type(ADD_ADMIN_FORM_NAME,     name);
        type(ADD_ADMIN_FORM_EMAIL,    email);
        type(ADD_ADMIN_FORM_PASSWORD, password);
        if (phone != null && !phone.isEmpty()) type(ADD_ADMIN_FORM_PHONE, phone);
    }

    public void submitAddAdminForm() {
        // FIX: use JS click — same reason as clickAddAdmin(). scrollAndClick
        // doesn't reliably trigger form submission in headless mode.
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('div.create-admin-form button.btn-primary').click();"
        );
    }

    public void cancelAddAdminForm() {
        scrollAndClick(driver.findElement(ADD_ADMIN_CANCEL));
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private boolean isVisible(By by) {
        try { return driver.findElement(by).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No '" + label + "' element visible");
        return els.get(0);
    }

    private void scrollAndClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }
}
