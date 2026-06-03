package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** Registration page at {@code /register}. */
public class RegisterPage extends BasePage {

    private static final By NAME      = By.cssSelector("input[type='text'].input");
    private static final By EMAIL     = By.cssSelector("input[type='email'].input");
    private static final By PHONE     = By.cssSelector("input[type='tel'].input");
    private static final By PASSWORD  = By.cssSelector("input[type='password'].input");
    private static final By ROLE_BTNS = By.cssSelector("button.role-btn");
    private static final By REGISTER_BTN = By.cssSelector("button.register-btn");
    private static final By LOGIN_LINK = By.cssSelector("a[routerlink='/login'], a[href='/login']");

    private static final By STORE_NAME = By.xpath("//input[@placeholder='Enter your store name']");

    // Field-level + page-level error surfaces. The Zuply SPA mixes Angular reactive-form
    // <mat-error>/<small class="error"> inline messages with global toast/alert banners
    // for backend rejections, so we cover both shapes with a single union selector.
    private static final By EMAIL_ERROR = By.cssSelector(
            "mat-error[for*='email'], small.error-message[for*='email']," +
            ".invalid-feedback.email, .email-error, [class*='email'][class*='error']");
    private static final By ALERT_BANNER = By.cssSelector(
            "[role='alert'], .toast, .alert, .alert-danger, .notification," +
            "[class*='toast'], [class*='snack'], [class*='banner'][class*='error']");

    public enum Role { CUSTOMER, SELLER }

    public RegisterPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/register"; }
    @Override protected By readyMarker() { return REGISTER_BTN; }

    // Every enter*() calls type() then verifies the input.value caught the
    // typed string. Defends against the Selenium-vs-Angular-rerender race
    // that occasionally drops characters and leaves the form invalid (which
    // keeps the register button [disabled] indefinitely).
    public RegisterPage enterName(String name)       { type(NAME, name);          waitForInputValue(NAME, name);          return this; }
    public RegisterPage enterEmail(String email)     { type(EMAIL, email);        waitForInputValue(EMAIL, email);        return this; }
    public RegisterPage enterPhone(String phone)     { type(PHONE, phone);        waitForInputValue(PHONE, phone);        return this; }
    public RegisterPage enterPassword(String pwd)    { type(PASSWORD, pwd);       waitForInputValue(PASSWORD, pwd);       return this; }
    public RegisterPage enterStoreName(String store) { type(STORE_NAME, store);   waitForInputValue(STORE_NAME, store);   return this; }
    public RegisterPage selectRole(Role role) {
        // Wait for at least one role button to render so we don't iterate an empty list.
        wait.until(ExpectedConditions.presenceOfElementLocated(ROLE_BTNS));
        List<WebElement> btns = driver.findElements(ROLE_BTNS);
        for (WebElement b : btns) {
            if (b.getText().trim().equalsIgnoreCase(role.name())) {
                // Scroll the role button into view then JS-click. The Zuply register page
                // has a hero section above the form, so role buttons sit below the fold
                // on smaller viewports -- a raw .click() lands at off-screen coordinates
                // and throws ElementClickInterceptedException at e.g. (573, -13).
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", b);
                return this;
            }
        }
        throw new IllegalStateException("Role button not found: " + role);
    }

    /**
     * Submit the register form.
     *
     * Pre-flight: confirm every required field actually carries a value before
     * waiting on the [disabled] binding. Without this, a blank field (caused
     * by a Selenium sendKeys race) leaves the form invalid and we time out at
     * 25s on a button that never enables. The pre-flight runs in milliseconds
     * when typing succeeded; it only fires the diagnostic path when the
     * verified type() couldn't recover the value.
     *
     * Waits up to {@code DEFAULT_WAIT} (25s) for the [disabled] binding to flip
     * — Angular's async email-uniqueness validator exceeds shorter waits on
     * Render cold-starts. Falls back to a JS click if an overlay intercepts.
     */
    public void submit() {
        assertFieldNotBlank(NAME,     "name");
        assertFieldNotBlank(EMAIL,    "email");
        assertFieldNotBlank(PHONE,    "phone");
        assertFieldNotBlank(PASSWORD, "password");
        WebElement btn = waitClickable(REGISTER_BTN, DEFAULT_WAIT);
        try { btn.click(); }
        catch (org.openqa.selenium.ElementClickInterceptedException e) { jsClick(btn); }
    }

    private void assertFieldNotBlank(By by, String fieldName) {
        if (driver.findElements(by).isEmpty()) return; // optional fields (e.g. STORE_NAME) may be absent
        String v = driver.findElement(by).getDomProperty("value");
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "RegisterPage." + fieldName + " field is blank at submit — sendKeys did not stick. "
                    + "Aborting before the 25s disabled-button timeout.");
        }
    }

    public void registerAs(String name, String email, String phone, String password, Role role) {
        enterName(name).enterEmail(email).enterPhone(phone).enterPassword(password).selectRole(role).submit();
    }

    public void registerAs(String name, String email, String phone, String password, Role role, String storeName) {
        enterName(name).enterEmail(email).enterPhone(phone).enterPassword(password);
        selectRole(role);
        enterStoreName(storeName); // Fill this after clicking Seller exposes the field
        submit();
    }

    public void goToLogin() { click(LOGIN_LINK); }

    /**
     * Wait until the SPA finishes post-register routing — the URL must leave
     * {@code /register}. After a successful submit the SPA either auto-logs the
     * user in or redirects to {@code /login}; either way the path changes.
     */
    public void waitForRegistrationToComplete() {
        wait.until(d -> {
            String url = d.getCurrentUrl();
            return url != null && !url.contains("/register");
        });
    }

    /** True when an inline email-field validation message is rendered. */
    public boolean hasEmailValidationError() { return exists(EMAIL_ERROR); }

    /** Text of the inline email validation message, or empty string if none. */
    public String getEmailErrorMessage() {
        return exists(EMAIL_ERROR) ? text(EMAIL_ERROR) : "";
    }

    /** True when a page-level alert/toast banner is visible (backend rejection surface). */
    public boolean hasAlertBanner() { return exists(ALERT_BANNER); }

    /** Text of the first visible alert/toast banner, or empty string if none. */
    public String getAlertBannerText() {
        return exists(ALERT_BANNER) ? text(ALERT_BANNER) : "";
    }
}
