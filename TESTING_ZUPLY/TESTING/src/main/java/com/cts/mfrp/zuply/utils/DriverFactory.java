package com.cts.mfrp.zuply.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public final class DriverFactory {

    private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();

    private DriverFactory() {}

    /** Returns the driver stored in the current thread, or null if none is set. */
    public static WebDriver current() { return TL.get(); }

    public static void setDriver(WebDriver d) {
        if (d == null) TL.remove(); else TL.set(d);
    }

    public static WebDriver get() {
        WebDriver d = TL.get();
        if (d == null) {
            d = create(true);
            TL.set(d);
        }
        return d;
    }

    public static WebDriver create(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (headless) opts.addArguments("--headless=new");
        opts.addArguments("--no-sandbox", "--disable-gpu", "--window-size=1366,900",
                "--disable-dev-shm-usage", "--remote-allow-origins=*");
        // pageLoadStrategy=eager: ready as soon as DOMContentLoaded fires; don't
        // wait for stragglers (chat FAB iframes, analytics, AI background calls)
        // which were causing "Timed out receiving message from renderer" in the
        // AI listing tests.
        opts.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER);
        WebDriver d = new ChromeDriver(opts);
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
        return d;
    }

    public static void quit() {
        WebDriver d = TL.get();
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) {}
            TL.remove();
        }
    }
}
