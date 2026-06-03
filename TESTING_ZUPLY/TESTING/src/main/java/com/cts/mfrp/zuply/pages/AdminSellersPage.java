package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Admin → Manage Sellers at {@code /admin/sellers}. */
public class AdminSellersPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[contains(normalize-space(), 'Manage Sellers')]");
    private static final By SEARCH_INPUT = By.cssSelector("input.search-input");
    private static final By FILTER_TABS  = By.cssSelector("button.filter-tab");
    private static final By APPROVE_BTNS = By.xpath("//button[contains(@class,'btn-primary') and normalize-space()='Approve']");
    private static final By REJECT_BTNS  = By.xpath("//button[contains(@class,'btn-warning') and normalize-space()='Reject']");
    private static final By SUSPEND_BTNS = By.xpath("//button[contains(@class,'btn-danger') and normalize-space()='Suspend']");

    // DevTools confirmed sellers are rendered as cards inside div.sellers-table
    private static final By TABLE_ROWS      = By.cssSelector("div.sellers-table > div");
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");

    public enum Filter {
        ALL_SELLERS("ALL SELLERS"),
        APPROVED("APPROVED"),
        PENDING_APPROVAL("PENDING APPROVAL"),
        SUSPENDED("SUSPENDED"),
        REJECTED("REJECTED");

        public final String tabLabel;
        Filter(String tabLabel) { this.tabLabel = tabLabel; }
    }

    public AdminSellersPage(WebDriver driver) { super(driver); }

    @Override public String route()        { return "/admin/sellers"; }
    @Override protected By readyMarker()  { return HEADING; }

    public AdminSellersPage search(String text) { type(SEARCH_INPUT, text); return this; }

    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.tabLabel)) {
                scrollAndClick(tab);
                return;
            }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    /**
     * Reads the count directly from the filter tab label.
     * e.g. "Approved 51" → 51, "Suspended 7" → 7
     * More reliable than counting DOM elements — reflects server-side total
     * regardless of pagination or how many cards are rendered on screen.
     */
    public int getFilterCount(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.tabLabel)) {
                String numbers = tab.getText().replaceAll("[^0-9]", "").trim();
                return numbers.isEmpty() ? 0 : Integer.parseInt(numbers);
            }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    public int rowCount()         { return driver.findElements(TABLE_ROWS).size(); }
    public int approvableCount()  { return driver.findElements(APPROVE_BTNS).size(); }
    public int suspendableCount() { return driver.findElements(SUSPEND_BTNS).size(); }
    public int rejectableCount()  { return driver.findElements(REJECT_BTNS).size(); }

    public void approveFirst() { scrollAndClick(firstOf(APPROVE_BTNS, "Approve")); }
    public void rejectFirst()  { scrollAndClick(firstOf(REJECT_BTNS,  "Reject")); }
    public void suspendFirst() { scrollAndClick(firstOf(SUSPEND_BTNS, "Suspend")); }

    public boolean isSellerVisible(String nameOrEmail) {
        return driver.findElements(TABLE_ROWS).stream()
                .anyMatch(row -> row.getText().contains(nameOrEmail));
    }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No " + label + " button visible");
        return els.get(0);
    }

    private void scrollAndClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }
}