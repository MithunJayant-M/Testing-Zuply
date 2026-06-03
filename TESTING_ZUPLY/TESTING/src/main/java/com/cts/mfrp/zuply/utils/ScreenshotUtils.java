package com.cts.mfrp.zuply.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ScreenshotUtils {

    private static final String SCREENSHOT_DIR = "reports/screenshots/";

    private ScreenshotUtils() {}

    /** Captures a screenshot and returns the file path (relative to project root). */
    public static String capture(WebDriver driver, String testName) throws IOException {
        new File(SCREENSHOT_DIR).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String sanitized = testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String path = SCREENSHOT_DIR + sanitized + "_" + timestamp + ".png";
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), Paths.get(path));
        return path;
    }

    /** Captures a screenshot and returns it as a Base64 string (for inline embedding in reports). */
    public static String captureBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
}
