package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Seller's customer-orders page at {@code /seller/orders}.
 * Order status enum: PROCESSING, SHIPPED, DELIVERED.
 */
public class SellerOrdersPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='Customer Orders']");
    private static final By ORDER_ROWS    = By.cssSelector(".order-card, .order-row, table tbody tr");
    private static final By TABLE_ROWS    = By.cssSelector(".table-row:not(.header)");
    private static final By ACTION_BTNS   = By.cssSelector(".action-btns .btn-primary.btn-sm.btn-pill");
    private static final By STATUS_DROPDOWNS = By.cssSelector("select.status-select, select[name='status'], select.select");
    private static final By UPDATE_BTNS   = By.xpath("//button[contains(translate(.,'UPDATE','update'),'update')]");

    /**
     * Filter tabs may be rendered as {@code <button class="filter-tab">}, generic
     * {@code .filter-tab} elements, or hidden when no orders exist. We accept any
     * of these shapes.
     */
    private static final By[] FILTER_TAB_CANDIDATES = new By[] {
            By.cssSelector("button.filter-tab"),
            By.cssSelector(".filter-tab"),
            By.cssSelector("[class*='filter-tab']"),
            By.cssSelector("button[class*='filter']"),
            By.cssSelector(".filter-tabs button, .filters button")
    };

    public SellerOrdersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/orders"; }
    @Override protected By readyMarker() { return HEADING; }

    public int orderCount() { return driver.findElements(ORDER_ROWS).size(); }

    /** Highest filter-tab count across all known render shapes (0 when the page renders an empty state). */
    public int filterTabCount() {
        int max = 0;
        for (By c : FILTER_TAB_CANDIDATES) {
            int n = driver.findElements(c).size();
            if (n > max) max = n;
            if (max >= 4) break;
        }
        return max;
    }

    /**
     * Wait up to {@code timeout} for order rows ({@code .table-row:not(.header)}) to render.
     * Returns the row count after the wait (zero when none rendered by the deadline).
     */
    public int waitForOrderRowCount(Duration timeout) {
        try {
            List<WebElement> rows = new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(TABLE_ROWS));
            return rows.size();
        } catch (TimeoutException e) {
            return driver.findElements(TABLE_ROWS).size();
        }
    }

    /** Count of per-row status action buttons (Process / Deliver). Zero when no orders exist. */
    public int actionButtonCount() { return driver.findElements(ACTION_BTNS).size(); }

    public void updateFirstOrderStatus(String status) {
        List<WebElement> dropdowns = driver.findElements(STATUS_DROPDOWNS);
        if (dropdowns.isEmpty()) throw new IllegalStateException("No status dropdown visible");
        new Select(dropdowns.get(0)).selectByVisibleText(status);
        List<WebElement> updateBtns = driver.findElements(UPDATE_BTNS);
        if (!updateBtns.isEmpty()) updateBtns.get(0).click();
    }
}
