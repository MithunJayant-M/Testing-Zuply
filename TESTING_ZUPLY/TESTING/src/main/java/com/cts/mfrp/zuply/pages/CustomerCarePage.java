package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Customer support / FAQ page at {@code /customer-care}. */
public class CustomerCarePage extends BasePage {

    private static final By HERO_TITLE    = By.cssSelector("h1.cc-hero-title");
    private static final By START_CHAT_BTN = By.cssSelector("button.cc-card-btn");
    private static final By SECTION_TITLES = By.cssSelector("h2.cc-section-title");

    public CustomerCarePage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/customer-care"; }
    @Override protected By readyMarker() { return HERO_TITLE; }

    public String heroTitle()        { return text(HERO_TITLE); }
    public void clickStartChat()     { click(START_CHAT_BTN); }
    public int sectionCount()        { return driver.findElements(SECTION_TITLES).size(); }
}
