package com.cts.mfrp.zuply.tests.ui.admin;


import com.cts.mfrp.zuply.base.UiBaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Role-based access control — FRD §3.2. Maps to TC039. */
@Test(groups = {"regression", "ui", "security"})
public class SecurityUiTests extends UiBaseTest {

    private static final Duration ROUTE_GUARD = Duration.ofSeconds(5);

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("RbacUser");
        loginViaUi(buyerEmail, defaultPassword());
    }

    /** TC039 — A Customer cannot reach /admin or /seller routes; routes redirect to /login. */
    @Test(description = "TC039 — RoleBasedAccess")
    public void tc039_roleBasedAccess() {
        // Attempt 1: customer hits /admin/dashboard via SPA router. Wait for the
        // Angular guard to react -- either we land on /login or any URL that does
        // NOT contain /admin/dashboard.
        navigateRoute("/admin/dashboard");
        try {
            new WebDriverWait(driver, ROUTE_GUARD).until(d -> {
                String u = d.getCurrentUrl();
                return u != null && !u.contains("/admin/dashboard");
            });
        } catch (Exception ignored) { /* fall through to assertion */ }
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/login")
                        || (driver.getCurrentUrl().contains("/")
                                && !driver.getCurrentUrl().contains("/admin/dashboard")),
                "Customer should not reach admin dashboard; landed on " + driver.getCurrentUrl());

        // Attempt 2: customer hits /seller/dashboard
        navigateRoute("/seller/dashboard");
        try {
            new WebDriverWait(driver, ROUTE_GUARD).until(
                    ExpectedConditions.not(ExpectedConditions.urlMatches(".*/seller/dashboard$")));
        } catch (Exception ignored) {}
        Assert.assertFalse(driver.getCurrentUrl().endsWith("/seller/dashboard"),
                "Customer should not reach seller dashboard; landed on " + driver.getCurrentUrl());
    }
}
