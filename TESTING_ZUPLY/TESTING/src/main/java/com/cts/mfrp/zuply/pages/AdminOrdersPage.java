package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Admin → All Orders at {@code /admin/orders}. */
public class AdminOrdersPage extends BasePage {

    private static final By HEADING     = By.xpath("//h1[contains(normalize-space(), 'All Orders')]");
    private static final By FILTER_TABS = By.cssSelector("button.filter-tab");
    private static final By ORDER_ROWS  = By.cssSelector("table tbody tr, .order-row, .card");

    public enum Filter { ALL, PLACED, PROCESSING, DELIVERED, CANCELLED }

    public AdminOrdersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/admin/orders"; }
    @Override protected By readyMarker() { return HEADING; }

    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.name())) { tab.click(); return; }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    public int orderCount() { return driver.findElements(ORDER_ROWS).size(); }
}