package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Seller's own products at {@code /seller/products}. */
public class SellerProductsPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='My Products']");
    private static final By PRODUCT_ROWS  = By.cssSelector(".product-card, .product-row, table tbody tr, [class*='product-item'], div.prod-card");
    private static final By PROD_CARDS    = By.cssSelector("div.prod-card");
    private static final By EDIT_BTNS     = By.xpath("//button[contains(translate(.,'EDIT','edit'),'edit')]");
    private static final By DELETE_BTNS   = By.xpath("//button[contains(translate(.,'DELETE','delete'),'delete')]");
    private static final By ADD_NEW_LINK  = By.cssSelector(
            "a[routerlink='/seller/upload'], a[href='/seller/upload'], "
            + "a.btn-accent.btn-pill[routerlink='/seller/upload'], a.btn-accent.btn-pill[href='/seller/upload']");
    private static final By EMPTY_STATE   = By.xpath("//*[contains(.,'No products') or contains(.,'empty')]");

    public SellerProductsPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/products"; }
    @Override protected By readyMarker() { return HEADING; }

    public int productCount() { return driver.findElements(PRODUCT_ROWS).size(); }
    public boolean isEmpty()  { return productCount() == 0; }

    /** Strict {@code div.prod-card} count, used by cross-seller isolation checks. */
    public int prodCardCount() { return driver.findElements(PROD_CARDS).size(); }

    public void clickAddNew() { click(ADD_NEW_LINK); }

    public void editFirst() {
        var els = driver.findElements(EDIT_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Edit button visible");
        els.get(0).click();
    }

    public void deleteFirst() {
        var els = driver.findElements(DELETE_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Delete button visible");
        els.get(0).click();
    }
}
