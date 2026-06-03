package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Login page at {@code /login}. */
public class LoginPage extends BasePage {

    private static final By EMAIL    = By.cssSelector("input[type='email'].input");
    private static final By PASSWORD = By.cssSelector("input[type='password'].input");
    private static final By LOGIN_BTN = By.cssSelector("button.login-btn");
    private static final By REGISTER_LINK = By.cssSelector("a[routerlink='/register'], a[href='/register']");

    public LoginPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/login"; }
    @Override protected By readyMarker() { return LOGIN_BTN; }

    /**
     * Type the email + verify the input.value actually carries it. The verify
     * step catches Selenium's occasional sendKeys-vs-Angular-rerender race
     * where some characters drop and the field is left blank or partial —
     * which leaves form.invalid=true and the submit button permanently disabled.
     */
    public LoginPage enterEmail(String email) {
        type(EMAIL, email);
        waitForInputValue(EMAIL, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD, password);
        waitForInputValue(PASSWORD, password);
        return this;
    }

    /**
     * Submit the login form.
     *
     * Pre-flight: confirms both fields actually carry a value (not blank/whitespace)
     * before waiting on the [disabled] binding. If a field is empty the test
     * fails fast with a clear "field is blank" message instead of waiting 25s
     * on a button that will never become clickable. The pre-flight runs in
     * milliseconds when the typing succeeded and only kicks in on the race
     * scenarios that the verified type() already self-heals.
     *
     * Waits up to {@code DEFAULT_WAIT} (25s) for the [disabled] binding to flip
     * — Angular's async validators can be slow on Render cold-starts. Falls
     * back to a JS click if an overlay intercepts the native click.
     */
    public void submit() {
        assertFieldNotBlank(EMAIL,    "email");
        assertFieldNotBlank(PASSWORD, "password");
        WebElement btn = waitClickable(LOGIN_BTN, DEFAULT_WAIT);
        try { btn.click(); }
        catch (org.openqa.selenium.ElementClickInterceptedException e) { jsClick(btn); }
    }

    private void assertFieldNotBlank(By by, String fieldName) {
        String v = driver.findElement(by).getDomProperty("value");
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "LoginPage." + fieldName + " field is blank at submit — sendKeys did not stick. "
                    + "Aborting before the 25s disabled-button timeout.");
        }
    }

    /** Convenience: fill + submit + wait for navigation away from /login. */
    public void loginAs(String email, String password) {
        enterEmail(email).enterPassword(password).submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    public void goToRegister() { click(REGISTER_LINK); }
}
