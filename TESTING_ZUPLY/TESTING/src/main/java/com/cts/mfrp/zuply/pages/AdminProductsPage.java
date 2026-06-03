package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Admin → Manage Products at {@code /admin/products}. */
public class AdminProductsPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[contains(normalize-space(), 'Manage Products')]");
    private static final By SEARCH_INPUT = By.cssSelector("input.search-input, input[placeholder*='Search']");
    private static final By FILTER_TABS  = By.cssSelector("button.filter-tab");

    // FIX: DevTools confirmed Approve = btn-primary btn-sm btn-pill
    private static final By APPROVE_BTNS = By.cssSelector("button.btn-primary.btn-sm.btn-pill");

    // FIX: DevTools confirmed Reject = btn-danger btn-sm btn-pill (NOT btn-warning as original had)
    private static final By REJECT_BTNS  = By.cssSelector("button.btn-danger.btn-sm.btn-pill");

    // FIX: DevTools confirmed product rows are div.table-row inside div.products-table
    // Exclude header row by adding :not(.header)
    private static final By PRODUCT_ROWS = By.cssSelector("div.products-table div.table-row:not(.header)");

    // Loading spinner
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");

    /**
     * FIX: Filter enum updated with tabLabel fields matching actual UI tab text.
     * Original had PENDING — UI says "Pending Review".
     * Uses tabLabel for matching so "PENDING REVIEW" matches "Pending Review 15".
     */
    public enum Filter {
        ALL_PRODUCTS("ALL PRODUCTS"),
        APPROVED("APPROVED"),
        PENDING_REVIEW("PENDING REVIEW"),   // FIX: was PENDING — tab says "Pending Review"
        REJECTED("REJECTED");

        public final String tabLabel;
        Filter(String tabLabel) { this.tabLabel = tabLabel; }
    }

    public AdminProductsPage(WebDriver driver) { super(driver); }

    @Override public String route()       { return "/admin/products"; }
    @Override protected By readyMarker() { return HEADING; }

    public AdminProductsPage search(String text) { type(SEARCH_INPUT, text); return this; }

    // FIX: Uses f.tabLabel instead of f.name() — resilient to count suffix e.g. "Pending Review 15"
    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.tabLabel)) {
                scrollAndClick(tab);
                return;
            }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    // FIX: was "table tr, .product-row, .card" — no <table> exists, .card too broad
    // Now counts only actual product rows excluding the header row
    public int rowCount() {
        return driver.findElements(PRODUCT_ROWS).size();
    }

    /**
     * Reads the count number directly from the filter tab label.
     * e.g. "Pending Review 15" → 15, "Approved 0" → 0
     * More reliable than counting DOM rows since it reflects server-side total.
     */
    public int getFilterCount(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.tabLabel)) {
                String text = tab.getText().replaceAll("[^0-9]", "").trim();
                return text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    public int approveCount() { return driver.findElements(APPROVE_BTNS).size(); }
    public int rejectCount()  { return driver.findElements(REJECT_BTNS).size(); }

    // FIX: uses scrollAndClick to avoid chat FAB interception
    public void approveFirst() { scrollAndClick(firstOf(APPROVE_BTNS, "Approve")); }
    public void rejectFirst()  { scrollAndClick(firstOf(REJECT_BTNS,  "Reject")); }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No " + label + " button visible");
        return els.get(0);
    }

    private void scrollAndClick(WebElement el) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }
}