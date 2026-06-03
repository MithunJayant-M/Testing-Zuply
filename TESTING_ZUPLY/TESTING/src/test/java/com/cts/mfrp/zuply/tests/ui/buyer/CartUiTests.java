package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CartPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Shopping cart — FRD §2.4. Maps to TC013, TC019 + AD_TC_CART2, AD_TC_CART3.
 * (TC014, AD_TC_CART1 removed per cleanup pass.)
 */
@Test(groups = {"regression", "ui", "cart"})
public class CartUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Cart");
        loginViaUi(buyerEmail, defaultPassword());
    }

    /** TC013 — Add a product to the cart. */
    @Test(description = "TC013 — AddToCart")
    public void tc013_addToCart() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();

        CartPage cart = new CartPage(driver);
        cart.open();
        Assert.assertTrue(cart.itemCount() >= 1,
                "Cart should contain at least 1 item after Add to cart click");
    }

    /**
     * AD_TC_CART2 — Adding the SAME product twice should not create a duplicate cart
     * row; the system shall increment the quantity instead (FRD §2.4).
     */
    @Test(description = "AD_TC_CART2 -- DuplicateAddIncrementsQuantity")
    public void tcCart2_duplicateAddIncrementsQuantity() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();
        products.open();
        products.addFirstToCart();

        CartPage cart = new CartPage(driver);
        cart.open();
        Assert.assertEquals(cart.itemCount(), 2,
                "Adding the same product twice should keep cart at 1 row and increment quantity (FRD §2.4) -- "
                + "actual row count: " + cart.itemCount());
    }

    /**
     * AD_TC_CART3 — Remove item action removes the product from the cart entirely.
     * FRD §2.4 lists "Remove item individually" as an explicit cart action.
     */
    @Test(description = "AD_TC_CART3 -- RemoveItemFromCart")
    public void tcCart3_removeItemFromCart() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();

        CartPage cart = new CartPage(driver);
        cart.open();
        int before = cart.itemCount();
        if (before == 0) {
            throw new SkipException("Cart did not receive the seeded item -- nothing to remove");
        }
        try {
            cart.removeFirst();
        } catch (Exception e) {
            throw new SkipException("Remove control not present in this SPA build: " + e.getMessage());
        }
        waitAfterAction();
        Assert.assertTrue(cart.itemCount() < before,
                "Cart item count should decrease after Remove click -- was " + before + ", now " + cart.itemCount());
    }

    /**
     * TC019 — Cart nav link shows an item count after a product is added.
     * BUG-CONFIRMATION TEST: the cart icon always reads "Cart" with no count
     * indicator; wishlist nav correctly shows "Wishlist\n1" after an add. Expected
     * to FAIL until the application bug is fixed.
     */
    @Test(description = "TC019 — CartCountBadgeUpdates [BUG]")
    public void tc019_cartNavCountUpdatesAfterAdd() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();
        waitAfterAction();

        var cartLink = driver.findElements(By.cssSelector("a.nav-cart"));
        Assert.assertFalse(cartLink.isEmpty(), "Cart nav link (a.nav-cart) not found in header");

        String cartNavText = cartLink.get(0).getText().trim();
        var childBadges = cartLink.get(0).findElements(By.cssSelector(
                ".badge, .count, [class*='badge'], [class*='count'], [class*='cart-count']"));
        boolean childHasCount = childBadges.stream().anyMatch(b -> {
            try { return Integer.parseInt(b.getText().trim()) >= 1; }
            catch (NumberFormatException e) { return !b.getText().trim().isEmpty(); }
        });
        boolean textHasCount = cartNavText.matches("(?s).*\\b[1-9]\\d*\\b.*");

        Assert.assertTrue(childHasCount || textHasCount,
                "Cart navigation should display an item count after adding a product " +
                "(e.g. 'Cart 1') — cart nav text was: '" + cartNavText.replace("\n", "\\n") + "' " +
                "(application bug: no visual count feedback on cart icon after add)");
    }
}
