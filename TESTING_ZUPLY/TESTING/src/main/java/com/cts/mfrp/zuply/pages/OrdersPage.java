package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Authenticated buyer's order history at {@code /orders}. */
public class OrdersPage extends BasePage {

    private static final By HEADING       = By.cssSelector("h1.orders-title");
    private static final By ORDER_CARDS   = By.cssSelector(".order-card, .order-row, [class*='order-item']");
    private static final By FILTER_TABS   = By.cssSelector("button.filter-tab");
    private static final By EMPTY_MSG     = By.xpath("//*[contains(.,'No orders yet') or contains(.,'empty')]");

    public OrdersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/orders"; }
    @Override protected By readyMarker() { return HEADING; }

    public int orderCount()  { return driver.findElements(ORDER_CARDS).size(); }
    public boolean isEmpty() { return orderCount() == 0; }

    public void selectFilter(String label) {
        for (var t : driver.findElements(FILTER_TABS)) {
            if (t.getText().equalsIgnoreCase(label)) { t.click(); return; }
        }
        throw new IllegalStateException("Filter tab not found: " + label);
    }
}
