package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Authenticated buyer wishlist at {@code /wishlist}. */
public class WishlistPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='My Wishlist']");
    private static final By ITEMS         = By.cssSelector(".wish-card");
    private static final By REMOVE_BTNS   = By.xpath("//button[contains(translate(.,'REMOVEDEL','removedel'),'remove')]");
    private static final By ADD_CART_BTNS = By.xpath("//button[contains(translate(.,'ADDTOCART','addtocart'),'add to cart')]");

    public WishlistPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/wishlist"; }
    @Override protected By readyMarker() { return HEADING; }

    public int itemCount()   { return driver.findElements(ITEMS).size(); }
    public boolean isEmpty() { return itemCount() == 0; }

    public void removeFirst() {
        var els = driver.findElements(REMOVE_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Remove button visible");
        els.get(0).click();
    }

    public void moveFirstToCart() {
        var els = driver.findElements(ADD_CART_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No 'Add to cart' button visible");
        els.get(0).click();
    }
}
