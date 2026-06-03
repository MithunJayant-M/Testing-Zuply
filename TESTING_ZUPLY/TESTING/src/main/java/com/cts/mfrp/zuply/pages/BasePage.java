package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared base for all Page Objects. Wraps a WebDriver + WebDriverWait and exposes
 * common helpers (visit, fill, click, isDisplayed) so subclasses stay declarative.
 *
 * The Zuply site is an Angular SPA hosted at https://zuply.netlify.app/. Netlify
 * does NOT have an SPA fallback configured, so direct URL navigation to any route
 * other than "/" returns Netlify's 404 page. Always start at "/" and let the
 * Angular router handle in-app navigation (this base class does that for you).
 */
public abstract class BasePage {

    public static final String BASE_URL = "https://zuply.netlify.app";

    /**
     * Global default wait window for page objects. Bumped from 10s to 25s in
     * response to TimeoutException churn on Angular reactive-form submit buttons
     * (the [disabled] binding flips on async validators that can be slow on
     * Render cold-starts). Specific operations can still override via the
     * Duration-taking helpers below.
     */
    public static final Duration DEFAULT_WAIT = Duration.ofSeconds(25);
    public static final Duration LONG_WAIT    = Duration.ofSeconds(40);

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final WebDriverWait longWait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_WAIT);
        this.longWait = new WebDriverWait(driver, LONG_WAIT);
    }

    /** Subclasses declare the route they live at (e.g. "/login", "/admin/dashboard"). */
    public abstract String route();

    /** Subclasses declare the CSS selector for a top-level element that proves the page rendered. */
    protected abstract By readyMarker();

    /**
     * Boots the SPA at "/" if needed, then navigates to {@link #route()} via
     * the Angular router (history.pushState + popstate event). Waits for the
     * page's ready marker to appear.
     */
    public void open() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null || !currentUrl.startsWith(BASE_URL)) {
            driver.get(BASE_URL + "/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
        }
        if (!route().equals("/")) {
            ((JavascriptExecutor) driver).executeScript(
                    "const p = arguments[0];" +
                    "const a = document.querySelector('a[href=\"' + p + '\"], a[routerlink=\"' + p + '\"]');" +
                    "if (a) { a.click(); } else { history.pushState({}, '', p); window.dispatchEvent(new PopStateEvent('popstate')); }",
                    route());
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(readyMarker()));
    }

    public boolean isLoaded() {
        try { return driver.findElement(readyMarker()).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    protected WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    /**
     * Type into a field with stale-element resilience AND value verification.
     *
     * Three failure modes this method defends against, in increasing severity:
     *   1. The Zuply SPA re-renders reactive-form controls when validators fire,
     *      which can invalidate a WebElement reference between the locate and
     *      the sendKeys call → StaleElementReferenceException. We re-find on
     *      every attempt.
     *   2. Selenium's sendKeys can race with the same re-render and drop
     *      characters, leaving the input partial or empty. Without a check the
     *      next thing the test does is wait 25s on a [disabled] submit button
     *      that will never enable. We compare {@code el.getDomProperty("value")}
     *      to {@code text} and, if it doesn't match, JS-assign the full string
     *      directly.
     *   3. The input is hidden behind a custom Angular component that intercepts
     *      keystrokes and the underlying <input> never receives them. Same JS
     *      fallback covers this case.
     *
     * After every successful write we dispatch input/change/blur so Angular's
     * [(ngModel)] picks the value up and the form validators run — without
     * blur, the submit button's [disabled]="form.invalid" binding stays true
     * even with a perfectly valid value.
     */
    protected void type(By by, String text) {
        org.openqa.selenium.StaleElementReferenceException lastStale = null;
        String expected = text == null ? "" : text;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement el = waitVisible(by);
                el.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"),
                        org.openqa.selenium.Keys.BACK_SPACE);
                if (!expected.isEmpty()) {
                    el.sendKeys(expected);
                }

                // Verify the input actually carries the value we sent. If sendKeys
                // raced with a re-render the field can end up empty/partial — fall
                // back to JS-setting the value so downstream form validation can run.
                String actual = el.getDomProperty("value");
                if (!expected.equals(actual == null ? "" : actual)) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].value = arguments[1];", el, expected);
                }

                // Angular reactive forms only run validators + mark the control as
                // "touched" on blur. Without this, the [disabled] binding on submit
                // buttons (form.invalid) never flips to false, so waitClickable() on
                // the submit button times out even though the field values are valid.
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('blur', {bubbles:true}));",
                        el);
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                lastStale = e;
                // fall through to next attempt; waitVisible() will re-find the element
            }
        }
        throw lastStale;
    }

    /**
     * Wait until the DOM property {@code value} of the input located by {@code by}
     * equals {@code expected}. Bounded by the page's default wait. Useful as a
     * pre-flight before clicking a submit button gated on [disabled]=form.invalid
     * — confirms Angular has registered the typed value, so the test fails fast
     * with "field still blank" rather than after a 25s waitClickable timeout.
     */
    protected void waitForInputValue(By by, String expected) {
        String target = expected == null ? "" : expected;
        wait.until(d -> {
            try {
                String v = d.findElement(by).getDomProperty("value");
                return target.equals(v == null ? "" : v);
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false;
            }
        });
    }

    /**
     * Wait up to {@code timeout} for an element to become clickable (visible + enabled).
     * Use this for submit buttons gated on async form validators (e.g. backend
     * email-uniqueness checks) that can exceed the default 10s wait on slow envs.
     */
    protected WebElement waitClickable(By by, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.elementToBeClickable(by));
    }

    /**
     * Click an element. Tries a native click first, then falls back to a
     * JavaScript click if the SPA's chat FAB (or any other overlay) intercepts
     * the native click — a chronic problem on the Zuply SPA.
     */
    protected void click(By by) {
        WebElement el = waitClickable(by);
        try {
            el.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
        }
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    protected String text(By by) { return waitVisible(by).getText().trim(); }

    /** True when at least one element matching {@code by} is in the DOM (no wait). */
    public boolean exists(By by) { return !driver.findElements(by).isEmpty(); }

    /** Count of elements currently matching {@code by} (no wait). */
    public int count(By by) { return driver.findElements(by).size(); }

    /**
     * Wait up to {@code timeout} for {@code app-loading-spinner} (or generic spinner
     * variants) to disappear. Returns immediately if no spinner is present, fails
     * silently on timeout. Use after filter clicks / API-triggered actions.
     */
    public void waitForSpinnerGone(Duration timeout) {
        By spinners = By.cssSelector("app-loading-spinner, .spinner, [class*='spinner'], [class*='loading']");
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.invisibilityOfElementLocated(spinners));
        } catch (Exception ignored) {}
    }

    /** Wait up to {@code timeout} for the current URL to contain {@code fragment}. */
    public boolean waitForUrlContains(String fragment, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.urlContains(fragment));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Wait up to {@code timeout} for the current URL to no longer contain {@code fragment}. */
    public boolean waitForUrlNotContains(String fragment, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> { String u = d.getCurrentUrl(); return u != null && !u.contains(fragment); });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String currentUrl() { return driver.getCurrentUrl(); }
    public String title() { return driver.getTitle(); }
}
