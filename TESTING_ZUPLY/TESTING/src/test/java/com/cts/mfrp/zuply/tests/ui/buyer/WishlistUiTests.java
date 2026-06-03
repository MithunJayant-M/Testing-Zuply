package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import com.cts.mfrp.zuply.pages.WishlistPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Wishlist — FRD §2.8. Maps to TC020 (bug-confirmation).
 * (TC010, TC011, TC012, TC021 removed per cleanup pass.)
 */
@Test(groups = {"regression", "ui", "wishlist"})
public class WishlistUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("WishUser");
        loginViaUi(buyerEmail, defaultPassword());
    }

    /**
     * TC020 — Re-clicking the Wishlist button on an already-wishlisted product
     * removes it (toggle). BUG-CONFIRMATION TEST: re-clicking does NOT decrease
     * the wishlist count — the item is not removed; the application is supposed
     * to toggle (add/remove) but silently fails on the second click. Expected
     * to FAIL until the application bug is fixed.
     */
    @Test(description = "TC020 — WishlistToggleRemovesItem [BUG]")
    public void tc020_wishlistToggleRemovesItem() {
        clearSession();
        loginViaUi(buyerEmail, defaultPassword());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".account-btn")));

        // First click — add the item to wishlist
        new ProductsPage(driver).open();
        // Explicit wait for backend-driven product render before we read the card list.
        // Without this, a slow products API response leaves us with an empty findElements
        // result and a misleading "No product cards found" assertion failure.
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector(".card-body, .card-name"), 0));
        var cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found on products page");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        var wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(), "No Wishlist button found on product detail page");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='My Wishlist']")),
                ExpectedConditions.urlContains("/login")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "Wishlist not accessible");
        int countAfterFirstClick = new WishlistPage(driver).itemCount();
        Assert.assertTrue(countAfterFirstClick >= 1,
                "Precondition: wishlist should have at least 1 item after first add");

        // Second click on the same product — should toggle-remove the item
        new ProductsPage(driver).open();
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector(".card-body, .card-name"), 0));
        cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found (second visit)");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(), "No Wishlist button found on detail page (second visit)");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='My Wishlist']")),
                ExpectedConditions.urlContains("/login")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "Wishlist not accessible");
        int countAfterToggle = new WishlistPage(driver).itemCount();

        Assert.assertTrue(countAfterToggle < countAfterFirstClick,
                "Re-clicking the Wishlist button should remove the item (toggle behavior) — " +
                "count was " + countAfterFirstClick + " after add, still " + countAfterToggle + " after re-click " +
                "(application bug: re-clicking shows error / silently fails instead of removing)");
    }
}
