package com.cts.mfrp.zuply.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Static explicit-wait helpers for non-Page-Object code (utilities, listeners,
 * standalone scripts). Inside Page Objects, prefer the {@code waitVisible} /
 * {@code waitClickable} helpers exposed by {@code BasePage} — those reuse a
 * single {@code WebDriverWait} per page instance.
 *
 * Centralizing the wait-condition surface here means fixed pauses are never
 * the right answer: each method below already polls until the SPA reaches the
 * expected state, then returns as soon as it does.
 */
public final class WaitUtils {

    /**
     * Default wait window — bumped from 15s to 25s alongside the BasePage default
     * after TimeoutException churn on Angular reactive-form submit buttons (the
     * [disabled] binding flips on async validators that can be slow on Render
     * cold-starts). Callers can still pass a custom Duration to the overloads.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(25);

    private WaitUtils() {}

    /** Wait for an element to be present + visible in the DOM. */
    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForVisible(WebDriver driver, By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait for an element to be visible AND enabled (safe to click). */
    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForClickable(WebDriver driver, By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Wait for an element to disappear (or never have been visible). */
    public static boolean waitForInvisible(WebDriver driver, By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Wait until the current URL contains the given fragment (e.g. after route change). */
    public static boolean waitForUrlContains(WebDriver driver, String fragment, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.urlContains(fragment));
    }

    /** Wait until the current URL no longer contains the given fragment. */
    public static boolean waitForUrlNotContains(WebDriver driver, String fragment, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(d -> {
                    String url = d.getCurrentUrl();
                    return url != null && !url.contains(fragment);
                });
    }

    /** Wait until the page text matches the given regex (case-insensitive). */
    public static boolean waitForBodyText(WebDriver driver, String regex, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.textMatches(
                        By.tagName("body"),
                        java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE)));
    }
}
