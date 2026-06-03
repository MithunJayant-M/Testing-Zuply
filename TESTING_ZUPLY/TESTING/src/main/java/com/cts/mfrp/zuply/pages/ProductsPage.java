package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/** Public product listing at {@code /products}. */
public class ProductsPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='All Products']");
    private static final By SEARCH_INPUT  = By.cssSelector("input.search-input");
    private static final By SEARCH_BTN    = By.cssSelector("button.search-btn");
    private static final By SORT_SELECT   = By.cssSelector("select.sort-select");
    private static final By PRODUCT_CARDS = By.cssSelector("[class*='product-card'], .card, .grid > *");
    // The SPA's product listing renders the add button as just "Add" inside <div class="card-footer">.
    // The longer "add to cart" text only appears on the product detail page. Match both.
    private static final By ADD_TO_CART_BTNS = By.xpath(
            "//div[contains(@class,'card-footer')]//button"
            + " | //button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add to cart')]"
            + " | //button[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='add']");
    private static final By WISHLIST_ICONS   = By.cssSelector(".wishlist-icon, .heart-icon, [class*='wishlist']");

    public ProductsPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/products"; }
    @Override protected By readyMarker() { return HEADING; }

    public ProductsPage search(String keyword) {
        type(SEARCH_INPUT, keyword);
        click(SEARCH_BTN);
        return this;
    }

    public ProductsPage sortBy(String visibleOption) {
        new Select(waitVisible(SORT_SELECT)).selectByVisibleText(visibleOption);
        return this;
    }

    public int productCount() { return driver.findElements(PRODUCT_CARDS).size(); }

    public boolean hasAddToCartButtons() { return exists(ADD_TO_CART_BTNS); }
    public boolean hasWishlistIcons()    { return exists(WISHLIST_ICONS); }
    public boolean hasSortDropdown()     { return exists(SORT_SELECT); }

    /**
     * Click the first Add-to-cart button. Uses {@link BasePage#click(By)} so the
     * SPA's chat FAB overlay can't intercept the click. After firing, waits up
     * to 3s for a toast/notification or for the cart-count badge to render --
     * whichever resolves first; falls through silently if neither appears.
     */
    public void addFirstToCart() {
        click(ADD_TO_CART_BTNS);
        waitForAsyncCartOrWishlistConfirmation();
    }

    /** Click the first wishlist heart icon. Waits up to 3s for a wishlist confirmation toast. */
    public void addFirstToWishlist() {
        click(WISHLIST_ICONS);
        waitForAsyncCartOrWishlistConfirmation();
    }

    private static final By ASYNC_CONFIRMATION = By.cssSelector(
            "[role='alert'], .toast, .notification, [class*='toast'], [class*='snack'], a.nav-cart .badge, a.nav-wishlist .badge");

    private void waitForAsyncCartOrWishlistConfirmation() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(ASYNC_CONFIRMATION));
        } catch (Exception ignored) { /* no visible feedback -- caller checks cart/wishlist state next */ }
    }
}
