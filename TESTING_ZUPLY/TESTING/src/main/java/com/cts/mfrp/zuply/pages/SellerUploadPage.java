package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class SellerUploadPage extends BasePage {

    // --- Locators ---
    private static final By HEADING       = By.xpath("//h1[normalize-space()='Add Product']");
    private static final By UPLOAD_ZONE   = By.cssSelector(".upload-zone");
    private static final By FILE_INPUT    = By.cssSelector("input[type='file'][accept='image/jpeg,image/png']");
    private static final By TITLE_INPUT   = By.cssSelector("input[type='text'].input[placeholder*='Handmade']");
    private static final By DESC_AREA     = By.cssSelector("textarea.input[placeholder*='Describe your product']");
    private static final By PRICE_INPUT   = By.cssSelector("input[type='number'].input[placeholder='e.g. 299']");
    private static final By STOCK_INPUT   = By.cssSelector("input[type='number'].input[placeholder='e.g. 10'], input[type='number'].input[placeholder='e.g. 50']");
    private static final By VARIATIONS_INPUT = By.cssSelector("input[type='text'].input[placeholder*='Red, Blue']");
    private static final By SELECTS       = By.cssSelector("select");
    private static final By SUBMIT_BTN    = By.xpath("//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'publish') or contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'submit')]");

    // AI Specific Locators
    private static final By GENERATE_BTN  = By.xpath("//button[contains(text(), 'Generate Listing with AI')]");
    private static final By AI_TAGS       = By.cssSelector(".tag, .chip-tag, [class*='tag-']");
    private static final By AI_HIGHLIGHTS = By.cssSelector(".highlight, .highlight-item, [class*='highlight']");
    private static final By AI_MODE_CARD  = By.xpath("//*[contains(text(), 'AI Enhanced')]");
    private static final By AI_INFO_BANNER = By.xpath("//*[contains(text(), 'AI will generate title')]");

    // --- Constructor & Overrides ---
    public SellerUploadPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/upload"; }
    @Override protected By readyMarker() { return HEADING; }

    // --- Action Methods ---

    public SellerUploadPage selectAiMode() {
        click(AI_MODE_CARD);
        waitVisible(AI_INFO_BANNER);
        return this;
    }

    public SellerUploadPage uploadImage(File img) {
        driver.findElement(FILE_INPUT).sendKeys(img.getAbsolutePath());
        return this;
    }

    // Restored and updated Generate clicker
    public SellerUploadPage clickGenerate() {
        click(GENERATE_BTN);
        return this;
    }

    // Manual input fillers
    public SellerUploadPage enterTitle(String title)       { type(TITLE_INPUT, title); return this; }
    public SellerUploadPage enterDescription(String desc)  { type(DESC_AREA, desc); return this; }
    public SellerUploadPage enterPrice(String price)       { type(PRICE_INPUT, price); return this; }
    public SellerUploadPage enterStock(String stock)       { type(STOCK_INPUT, stock); return this; }
    public SellerUploadPage enterVariations(String vars)   { type(VARIATIONS_INPUT, vars); return this; }

    public SellerUploadPage selectOptionByText(String visibleText) {
        for (WebElement sel : driver.findElements(SELECTS)) {
            for (WebElement opt : sel.findElements(By.tagName("option"))) {
                if (opt.getText().trim().equalsIgnoreCase(visibleText)) {
                    new Select(sel).selectByVisibleText(opt.getText());
                    return this;
                }
            }
        }
        return this;
    }

    public void submitForReview() { click(SUBMIT_BTN); }

    // --- Getters & Accessors ---

    public List<WebElement> selects() { return driver.findElements(SELECTS); }

    public String generatedTitle() {
        try {
            String v = driver.findElement(TITLE_INPUT).getAttribute("value");
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    public int tagCount()       { return count(AI_TAGS); }
    public int highlightCount() { return count(AI_HIGHLIGHTS); }

    public String firstSelectValue() {
        List<WebElement> sels = driver.findElements(SELECTS);
        if (sels.isEmpty()) return "";
        String v = sels.get(0).getAttribute("value");
        return v == null ? "" : v;
    }

    public boolean hasAiContentGenerated() {
        return !generatedTitle().isBlank() || tagCount() > 0 || highlightCount() > 0;
    }

    // --- Wait / Synchronization Helpers ---

    public WebElement waitForSubmitClickable() {
        return longWait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
    }

    public List<WebElement> waitForSelectCount(int expected) {
        return longWait.until(ExpectedConditions.numberOfElementsToBe(SELECTS, expected));
    }

    public boolean waitForAiContent(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(d -> hasAiContentGenerated());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForUploadAccepted(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(d -> {
                if (hasAiContentGenerated()) return true;
                return !driver.findElements(TITLE_INPUT).isEmpty();
            });
        } catch (Exception ignored) {}
    }
}