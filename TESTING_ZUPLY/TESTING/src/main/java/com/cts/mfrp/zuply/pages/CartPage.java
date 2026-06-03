package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Authenticated buyer cart at {@code /cart}. */
public class CartPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[normalize-space()='Shopping Cart']");
    private static final By CART_ITEMS   = By.cssSelector(".cart-item, [class*='cart-item']");
    private static final By QTY_INPUTS   = By.cssSelector("input[type='number'], .qty-input");
    private static final By QTY_PLUS_BTNS = By.xpath("//button[normalize-space()='+'] | //button[contains(@class,'qty-plus')]");
    private static final By REMOVE_BTNS  = By.xpath("//button[contains(translate(.,'REMOVEDEL','removedel'),'remove')]");
    private static final By GRAND_TOTAL  = By.cssSelector(".grand-total, [class*='total']");
    private static final By CHECKOUT_BTN = By.xpath("//button[contains(translate(.,'CHECKOUT','checkout'),'checkout')]");
    private static final By EMPTY_STATE  = By.xpath("//*[contains(.,'empty') or contains(.,'no items')]");

    public CartPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/cart"; }
    @Override protected By readyMarker() { return HEADING; }

    public int itemCount()      { return driver.findElements(CART_ITEMS).size(); }
    public boolean isEmpty()    { return itemCount() == 0; }
    public String grandTotal()  { return text(GRAND_TOTAL); }

    public void removeFirst() {
        var els = driver.findElements(REMOVE_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Remove button visible");
        els.get(0).click();
    }

    public void setFirstQuantity(String quantity) {
        var els = driver.findElements(QTY_INPUTS);
        if (els.isEmpty()) throw new IllegalStateException("No quantity input visible");
        WebElement el = els.get(0);
        el.clear();
        el.sendKeys(quantity);
    }

    public void proceedToCheckout() { click(CHECKOUT_BTN); }

    /**
     * Click the first quantity "+" button if one is present. Returns {@code true}
     * when the click was issued, {@code false} when the SPA renders no "+" button
     * (some builds use steppers, others a free-text input). Briefly polls for the
     * grand-total to refresh or a notification to surface; falls through silently.
     */
    public boolean incrementFirstQuantity() {
        if (!exists(QTY_PLUS_BTNS)) return false;
        click(QTY_PLUS_BTNS);
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(2))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("[role='alert'], .toast, [class*='snack'], .grand-total, [class*='total']")));
        } catch (Exception ignored) {}
        return true;
    }
}
