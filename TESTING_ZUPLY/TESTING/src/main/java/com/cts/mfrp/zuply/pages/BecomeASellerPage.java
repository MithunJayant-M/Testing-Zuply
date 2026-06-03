package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Marketing/landing page at {@code /become-a-seller}. */
public class BecomeASellerPage extends BasePage {

    private static final By HEADING            = By.cssSelector("h1.bas-heading");
    private static final By CREATE_ACCOUNT_BTN = By.xpath("//button[contains(normalize-space(), 'Create Seller Account')]");
    private static final By GET_STARTED_BTN    = By.xpath("//button[contains(normalize-space(), \"Get Started\")]");

    public BecomeASellerPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/become-a-seller"; }
    @Override protected By readyMarker() { return HEADING; }

    public String headingText()         { return text(HEADING); }
    public void clickCreateAccount()    { click(CREATE_ACCOUNT_BTN); }
    public void clickGetStarted()       { click(GET_STARTED_BTN); }
}
