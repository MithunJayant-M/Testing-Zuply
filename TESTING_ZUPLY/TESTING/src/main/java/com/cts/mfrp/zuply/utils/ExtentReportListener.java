package com.cts.mfrp.zuply.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener, ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        // Pick a report sub-directory before BaseTest's @BeforeSuite calls
        // ExtentManager.get() and initialises the singleton. Suite XML may
        // override via <parameter name="report.subdir" value="..."/>; otherwise
        // we derive it from the suite name using word-boundary matching so
        // "ui" inside "sUIte" (or "api" inside other words) doesn't false-match.
        String explicit = suite.getXmlSuite() != null
                ? suite.getXmlSuite().getParameter("report.subdir") : null;
        if (explicit != null && !explicit.isBlank()) {
            ExtentManager.setSubdir(explicit.trim());
            return;
        }
        String name = suite.getName() == null ? "" : suite.getName().toLowerCase();
        if (name.matches(".*\\bui\\b.*")) {
            ExtentManager.setSubdir("ui");
        } else if (name.matches(".*\\bapi\\b.*")) {
            ExtentManager.setSubdir("api");
        } else {
            ExtentManager.setSubdir("misc");
        }
    }

    @Override
    public void onStart(ITestContext context) {}

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getRealClass().getSimpleName()
                + " :: " + result.getMethod().getMethodName();
        ExtentTest test = ExtentManager.get().createTest(testName, result.getMethod().getDescription());

        // Assign category based on package (api vs ui) and groups
        String pkg = result.getTestClass().getRealClass().getPackage().getName();
        if (pkg.contains(".ui.")) {
            test.assignCategory("UI");
        } else if (pkg.contains(".api.")) {
            test.assignCategory("API");
        }
        String[] groups = result.getMethod().getGroups();
        for (String g : groups) {
            test.assignCategory(g);
        }

        ExtentManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (ExtentManager.test() != null) {
            ExtentManager.test().pass("Passed");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.test();
        if (test != null) {
            test.fail(result.getThrowable());
            // Capture screenshot for UI tests (driver is stored in DriverFactory ThreadLocal by UiBaseTest)
            WebDriver driver = DriverFactory.current();
            if (driver != null) {
                try {
                    String base64 = ScreenshotUtils.captureBase64(driver);
                    test.fail("Failure screenshot",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                } catch (Exception e) {
                    test.warning("Could not capture screenshot: " + e.getMessage());
                }
            }
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.test() != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Skipped — dependency failed or SkipException thrown";
            ExtentManager.test().skip(reason);
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.get().flush();
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.get().flush();
    }
}
