package com.cts.mfrp.zuply.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * Singleton ExtentReports + per-thread ExtentTest holder.
 *
 * The output path is segmented by suite type (api / ui / misc) so API and UI
 * runs never overwrite each other. Call {@link #setSubdir(String)} BEFORE the
 * first {@link #get()} (the listener does this from ISuiteListener.onStart).
 */
public final class ExtentManager {

    private static ExtentReports extent;
    private static volatile String subdir;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    private ExtentManager() {}

    /**
     * Set the report sub-directory (e.g. "api", "ui"). No-op if the reporter has
     * already been initialised — only the first caller wins per JVM lifetime.
     */
    public static synchronized void setSubdir(String value) {
        if (extent != null) return;
        subdir = value;
    }

    public static synchronized ExtentReports get() {
        if (extent == null) {
            String basePath = ConfigReader.get("report.path");
            String path = insertSubdir(basePath, subdir);
            File outFile = new File(path);
            File parent = outFile.getParentFile();
            if (parent != null) parent.mkdirs();

            String label = (subdir == null || subdir.isBlank()) ? "Test" : subdir.toUpperCase();
            ExtentSparkReporter spark = new ExtentSparkReporter(path);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Zuply " + label + " Test Report");
            spark.config().setReportName("Zuply " + label + " Automation");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Base URL", ConfigReader.get("base.url"));
            extent.setSystemInfo("Environment", ConfigReader.get("env"));
            extent.setSystemInfo("Suite Type", label);
        }
        return extent;
    }

    /**
     * Inserts {@code sub} as a folder between the parent directory and the file
     * name. {@code reports/ExtentReport.html} + "api" -> {@code reports/api/ExtentReport.html}.
     */
    private static String insertSubdir(String basePath, String sub) {
        if (sub == null || sub.isBlank()) return basePath;
        String normalized = basePath.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash < 0) {
            return sub + "/" + normalized;
        }
        return normalized.substring(0, slash) + "/" + sub + normalized.substring(slash);
    }

    public static void setTest(ExtentTest t) { currentTest.set(t); }
    public static ExtentTest test()           { return currentTest.get(); }
    public static void removeTest()           { currentTest.remove(); }
}
