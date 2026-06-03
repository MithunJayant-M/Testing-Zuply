package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Authenticated buyer checkout at {@code /checkout}. Fields are
 * placeholder-driven (no name attributes). Backend's DeliveryAddressDto =
 * customerName / phone / address / city / pincode.
 */
public class CheckoutPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='Checkout']");
    // The form puts customer name in the first text input; address in the textarea (placeholder
    // "House no..."), then city, pincode, phone (all .input class with distinct placeholders).
    private static final By NAME_INPUT    = By.cssSelector("input[type='text'].input:not([placeholder='City']):not([placeholder='600001'])");
    private static final By ADDRESS_AREA  = By.cssSelector("textarea.input[placeholder^='House no']");
    private static final By CITY_INPUT    = By.cssSelector("input.input[placeholder='City']");
    private static final By PINCODE_INPUT = By.cssSelector("input.input[placeholder='600001']");
    private static final By PHONE_INPUT   = By.cssSelector("input[type='tel'].input[placeholder='9876543210']");
    private static final By PAYMENT_RADIOS = By.cssSelector("input[type='radio']");
    private static final By PAYMENT_OPTIONS = By.cssSelector(".payment-option, .payment-method, label.payment");
    private static final By PLACE_ORDER_BTN = By.xpath("//button[contains(., 'Place Order')]");


    private static final By RAZORPAY_IFRAME = By.cssSelector("iframe.razorpay-checkout-frame");
    private static final By RPAY_NETBANKING_BTN = By.xpath("//button[descendant::div[text()='Netbanking']]");
    private static final By RPAY_TEST_BANK_BTN = By.xpath("//div[contains(text(), 'ICICI')]"); // Or any bank visible in the test modal
    private static final By RPAY_PAY_NOW_BTN = By.xpath("//button[@id='redesign-v15-cta']");
    private static final By RPAY_SUCCESS_MOCK_BTN = By.xpath("//button[contains(text(), 'Success')]");

    private static final By SUCCESS_MESSAGE = By.xpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'order placed') or contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'success')]");
    private static final By CITY_ERROR = By.xpath("//*[contains(text(), 'Please enter your city') or contains(text(), 'City is required')]");

    private static final By RPAY_PHONE_INPUT = By.cssSelector("input[type='tel']");
    private static final By RPAY_CONTINUE_BTN = By.xpath("//button[contains(., 'Continue')]");


    public CheckoutPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/checkout"; }
    @Override protected By readyMarker() { return HEADING; }

    public CheckoutPage fillAddress(String name, String phone, String address, String city, String pincode) {
        // Customer name is the first plain-text input
        var nameEls = driver.findElements(By.cssSelector("input[type='text'].input"));
        if (!nameEls.isEmpty()) { nameEls.get(0).clear(); nameEls.get(0).sendKeys(name); }
        type(ADDRESS_AREA, address);
        type(CITY_INPUT, city);
        type(PINCODE_INPUT, pincode);
        type(PHONE_INPUT, phone);
        return this;
    }

    public CheckoutPage selectPaymentMethod(String method) {
        // Use jsClick instead of native click to force Selenium to scroll down first!
        for(WebElement r : driver.findElements(PAYMENT_RADIOS)) {
            String v = r.getAttribute("value");
            if(method.equalsIgnoreCase(v)) {
                jsClick(r);
                return this;
            }
        }
        for(WebElement opt : driver.findElements(PAYMENT_OPTIONS)) {
            if(opt.getText().toUpperCase().contains(method.toUpperCase())) {
                jsClick(opt);
                return this;
            }
        }
        throw new IllegalStateException("Payment method not found: " + method);
    }

    // --- 1. FIXED VALIDATION CHECKERS (Must check for visibility!) ---
    public boolean isOrderSuccessMessageVisible() {
        // Only return true if the element is physically visible on the screen
        return driver.findElements(SUCCESS_MESSAGE).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean isCityValidationErrorVisible() {
        // Only return true if the element is physically visible on the screen
        return driver.findElements(CITY_ERROR).stream().anyMatch(WebElement::isDisplayed);
    }


    /**
     * Verifies that the Razorpay modal successfully opened.
     * We do not interact with the inside of the modal to avoid
     * third-party anti-bot security blocks.
     */
    public boolean isRazorpayModalOpened() {
        try {
            // Wait up to 10 seconds for the Razorpay iframe to appear on the screen
            longWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RAZORPAY_IFRAME));

            // If we successfully switched into it, it exists! Switch back and return true.
            driver.switchTo().defaultContent();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    /**
//     * Handles the Razorpay test payment flow.
//     * Switches to the iframe, bypasses the contact overlay if present,
//     * simulates a Netbanking success payment, and switches back.
//     */
//    public void handleRazorpayTestPayment() {
//        // Wait for and switch to the Razorpay iframe
//        longWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RAZORPAY_IFRAME));
//
//        try {
//            // 1. SMART WAIT FOR CONTACT OVERLAY
//            try {
//                // Wait for the inputs to load
//                new WebDriverWait(driver, java.time.Duration.ofSeconds(3))
//                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='tel'], input[placeholder*='Mobile']")));
//
//                // Find the actual VISIBLE phone input on the screen
//                WebElement visiblePhoneInput = null;
//                for (WebElement input : driver.findElements(By.cssSelector("input[type='tel'], input[placeholder*='Mobile']"))) {
//                    if (input.isDisplayed() && input.isEnabled()) {
//                        visiblePhoneInput = input;
//                        break;
//                    }
//                }
//
//                if (visiblePhoneInput != null) {
//
//                    // THE HOLY GRAIL FIX: The React ValueTracker Bypass.
//                    // This resets React's hidden security cache before injecting the value.
//                    String reactBypassScript =
//                            "var input = arguments[0];" +
//                                    // 1. Override the native HTML value
//                                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
//                                    "nativeSetter.call(input, '9999999999');" +
//                                    // 2. Find and reset React's hidden security tracker
//                                    "var tracker = input._valueTracker;" +
//                                    "if (tracker) { tracker.setValue(''); }" +
//                                    // 3. Fire the events so React thinks a human typed it
//                                    "input.dispatchEvent(new Event('input', { bubbles: true }));" +
//                                    "input.dispatchEvent(new Event('change', { bubbles: true }));" +
//                                    "input.dispatchEvent(new Event('blur', { bubbles: true }));";
//
//                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(reactBypassScript, visiblePhoneInput);
//
//                    // Wait until React has applied the synthetic state change.
//                    // Click Continue natively
//                    WebElement continueBtn = driver.findElement(By.xpath("//button[contains(., 'Continue') or contains(., 'Proceed')]"));
//                    continueBtn.click();
//
//                    // Wait for the overlay to actually DISAPPEAR
//                    new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
//                            .until(ExpectedConditions.invisibilityOf(visiblePhoneInput));
//                }
//
//            } catch (Exception e) {
//                // The contact overlay never appeared, or failed to close safely.
//            }
//
//            // 2. Click "Netbanking" (Using jsClick)
//            WebElement netbankingBtn = longWait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//*[contains(text(), 'Netbanking') or contains(text(), 'Net Banking')]")));
//            jsClick(netbankingBtn);
//
//            // 3. Select a bank
//            WebElement bankBtn = longWait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//*[text()='ICICI' or text()='SBI' or text()='HDFC' or contains(text(), 'Bank of Baroda') or contains(@class, 'bank-logo')]")));
//            jsClick(bankBtn);
//
//            // 4. Click the final "Pay Now" button
//            WebElement payNowBtn = longWait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pay')]")));
//            jsClick(payNowBtn);
//
//            // 5. Razorpay often opens a secondary mock window to click "Success"
//            try {
//                WebElement successBtn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
//                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'Success')]")));
//                jsClick(successBtn);
//            } catch (Exception ignored) {}
//
//        } finally {
//            // ALWAYS switch back to the main window
//            driver.switchTo().defaultContent();
//        }
//    }

    public void placeOrder() { click(PLACE_ORDER_BTN); }
}
