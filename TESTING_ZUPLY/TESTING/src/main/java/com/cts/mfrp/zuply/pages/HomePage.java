package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Landing page at {@code /} — the only route Netlify serves directly. */
public class HomePage extends BasePage {

    private static final By NAV_BRAND       = By.cssSelector("a.nav-brand");
    private static final By HAMBURGER       = By.cssSelector("button.hamburger");
    private static final By LOGIN_LINK      = By.cssSelector("a[routerlink='/login'], a[href='/login']");
    private static final By REGISTER_LINK   = By.cssSelector("a[routerlink='/register'], a[href='/register']");
    private static final By PRODUCTS_LINK   = By.cssSelector("a[routerlink='/products'], a[href='/products']");
    private static final By CART_LINK       = By.cssSelector("a[routerlink='/cart'], a[href='/cart']");
    private static final By WISHLIST_LINK   = By.cssSelector("a[routerlink='/wishlist'], a[href='/wishlist']");
    private static final By SELLERS_LINK    = By.cssSelector("a[routerlink='/sellers'], a[href='/sellers']");
    private static final By BECOME_SELLER   = By.cssSelector("a[routerlink='/become-a-seller'], a[href='/become-a-seller']");
    private static final By CUSTOMER_CARE   = By.cssSelector("a[routerlink='/customer-care'], a[href='/customer-care']");
    private static final By HERO_PRIMARY_BTN = By.cssSelector("button.btn-hero-primary, a.btn-hero-primary");
    private static final By HERO_SECONDARY_BTN = By.cssSelector("button.btn-hero-secondary, a.btn-hero-secondary");
    private static final By CTA_PRIMARY     = By.cssSelector("button.btn-cta-primary, a.btn-cta-primary");
    private static final By SELLER_CTA      = By.cssSelector("button.btn-seller-cta, a.btn-seller-cta");
    private static final By CHAT_FAB        = By.cssSelector("button.chat-fab, .chat-fab-wrap");
    private static final By NEWSLETTER_INPUT = By.cssSelector("input.newsletter-input");
    private static final By NEWSLETTER_BTN   = By.cssSelector("button.newsletter-btn");

    public HomePage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/"; }
    @Override protected By readyMarker() { return NAV_BRAND; }

    public void goToLogin()        { click(LOGIN_LINK); }
    public void goToRegister()     { click(REGISTER_LINK); }
    public void goToProducts()     { click(PRODUCTS_LINK); }
    public void goToCart()         { click(CART_LINK); }
    public void goToWishlist()     { click(WISHLIST_LINK); }
    public void goToBecomeSeller() { click(BECOME_SELLER); }
    public void goToCustomerCare() { click(CUSTOMER_CARE); }
    public void openHamburger()    { click(HAMBURGER); }
    public void openChatFab()      { click(CHAT_FAB); }
    public void clickHeroPrimary() { click(HERO_PRIMARY_BTN); }
    public void clickHeroSecondary() { click(HERO_SECONDARY_BTN); }
    public void clickCtaPrimary()  { click(CTA_PRIMARY); }
    public void clickSellerCta()   { click(SELLER_CTA); }

    public void subscribeNewsletter(String email) {
        type(NEWSLETTER_INPUT, email);
        click(NEWSLETTER_BTN);
    }

    public boolean hasNavBrand()     { return exists(NAV_BRAND); }
    public boolean hasLoginLink()    { return exists(LOGIN_LINK); }
    public boolean hasRegisterLink() { return exists(REGISTER_LINK); }
    public boolean hasProductsLink() { return exists(PRODUCTS_LINK); }
    public boolean hasSellersLink()  { return exists(SELLERS_LINK); }
}
