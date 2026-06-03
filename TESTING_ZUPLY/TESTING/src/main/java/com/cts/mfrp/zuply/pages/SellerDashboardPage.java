package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Seller's home at {@code /seller/dashboard}. Shows 4 stat cards:
 * Total Products, Total Orders, Pending Orders, Approved Products.
 */
public class SellerDashboardPage extends BasePage {

    private static final By STATS_GRID    = By.cssSelector(".grid-4, .stat-card, h1, .dashboard-header");
    private static final By STAT_CARDS    = By.cssSelector(".stat-card");
    private static final By UPLOAD_LINK   = By.cssSelector("a[routerlink='/seller/upload']");
    private static final By PRODUCTS_LINK = By.cssSelector("a[routerlink='/seller/products']");
    private static final By ORDERS_LINK   = By.cssSelector("a[routerlink='/seller/orders']");

    public SellerDashboardPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/dashboard"; }
    @Override protected By readyMarker() { return STATS_GRID; }

    public String totalRevenue() {
        return statValue("Revenue");
    }
    /**
     * Click any anchor on the dashboard that points at the given route. Tries
     * several selector shapes because the SPA may render the quick-action card
     * as either an {@code <a href="...">} or {@code <a routerlink="...">}, and
     * the wrapping class has changed across builds (qaction-row, action-row,
     * nav-card, ...).
     */
    public void goToRoute(String route) {
        By[] candidates = new By[] {
                By.cssSelector("a[href='" + route + "']"),
                By.cssSelector("a[routerlink='" + route + "']"),
                By.cssSelector("a[href$='" + route + "']"),
                By.xpath("//a[contains(@href,'" + route + "') or contains(@routerlink,'" + route + "')]")
        };
        for (By c : candidates) {
            List<WebElement> els = driver.findElements(c);
            if (!els.isEmpty()) {
                try {
                    longWait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    return;
                } catch (TimeoutException ignored) { /* try next candidate */ }
            }
        }
        throw new AssertionError("No clickable dashboard link found for route: " + route);
    }

    /** Wait until the stat-card grid has rendered exactly {@code expected} cards. */
    public void waitForStatCards(int expected) {
        longWait.until(ExpectedConditions.numberOfElementsToBe(STAT_CARDS, expected));
    }

    /** Returns the numeric value (as text) shown under the given stat label. */
    public String statValue(String labelText) {
        for (WebElement card : driver.findElements(STAT_CARDS)) {
            try {
                WebElement label = card.findElement(By.cssSelector(".stat-label"));
                if (label.getText().trim().equalsIgnoreCase(labelText)) {
                    return card.findElement(By.cssSelector(".stat-value")).getText().trim();
                }
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Stat card not found: " + labelText);
    }

    public String totalProducts()    { return statValue("Total Products"); }
    public String totalOrders()      { return statValue("Total Orders"); }
    public String pendingOrders()    { return statValue("Pending Orders"); }
    public String approvedProducts() { return statValue("Approved Products"); }

    public int statCardCount() { return driver.findElements(STAT_CARDS).size(); }

    public void goToUpload()   { click(UPLOAD_LINK); }
    public void goToProducts() { click(PRODUCTS_LINK); }
    public void goToOrders()   { click(ORDERS_LINK); }
}
