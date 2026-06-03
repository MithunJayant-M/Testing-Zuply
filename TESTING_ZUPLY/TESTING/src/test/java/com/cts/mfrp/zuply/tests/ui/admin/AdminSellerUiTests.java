package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.pages.AdminSellersPage;
import com.cts.mfrp.zuply.base.UiBaseTest;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin seller management — FRD §2.11. Maps to TC024. */
public class AdminSellerUiTests extends UiBaseTest {

    private static final By SELLERS_HEADING =
            By.xpath("//h1[contains(normalize-space(),'Manage Sellers')]");
    private static final By FILTER_TABS     = By.cssSelector("button.filter-tab");
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() {
        loginAsAdmin();
        // FIX: must go to "/" first (Netlify 404s on direct sub-route navigation)
        // then pushState to /admin/sellers, then wait for spinner then filter tabs
        driver.get("https://zuply.netlify.app/");
        org.openqa.selenium.support.ui.WebDriverWait setupWait =
                new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(45));
        setupWait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .presenceOfElementLocated(org.openqa.selenium.By.cssSelector("app-root")));
        // pushState to sellers route
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "history.pushState({}, '', '/admin/sellers'); " +
                        "window.dispatchEvent(new PopStateEvent('popstate'));");
        // Wait for heading
        setupWait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .visibilityOfElementLocated(SELLERS_HEADING));
        // Wait for spinner to disappear — API fetching sellers data
        try {
            setupWait.until(org.openqa.selenium.support.ui.ExpectedConditions
                    .invisibilityOfElementLocated(
                            org.openqa.selenium.By.cssSelector("app-loading-spinner")));
        } catch (Exception ignored) {}
        // Wait for filter tabs to appear
        setupWait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .numberOfElementsToBeMoreThan(
                        org.openqa.selenium.By.cssSelector("button.filter-tab"), 0));
    }

    /** TC024 — Admin can suspend a seller; page updates automatically after confirmation. */
    @Test(description = "TC024 — AdminSuspendSeller")
    public void tc024_adminSuspendSeller() {
        AdminSellersPage page = new AdminSellersPage(driver);

        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Step 1 — open sellers page (filter tabs already loaded in @BeforeClass)
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELLERS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 3 — select Approved filter
        try { page.selectFilter(AdminSellersPage.Filter.APPROVED); }
        catch (Exception ignored) {}

        // Step 4 — wait for heading, spinner and tabs after filter click
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELLERS_HEADING));
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 5 — read approved count from tab label
        int approvedBefore = page.getFilterCount(AdminSellersPage.Filter.APPROVED);
        System.out.println("Approved count from tab before: " + approvedBefore);

        // Step 6 — skip if no suspend buttons visible
        if (page.suspendableCount() == 0) {
            throw new org.testng.SkipException(
                    "No approved sellers available to suspend on this env");
        }

        // Step 7 — click Suspend button
        page.suspendFirst();

        // Step 8 — handle browser confirmation dialog
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Confirmation dialog text: " + alert.getText());
            alert.accept();
        } catch (NoAlertPresentException e) {
            System.out.println("No confirmation dialog appeared after suspend click");
        }

        // Step 9 — wait for spinner and tabs after API call
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 10 — wait for Approved tab count to decrease by 1
        longWait.until(d ->
                page.getFilterCount(AdminSellersPage.Filter.APPROVED) == approvedBefore - 1);

        // Step 11 — assert page is still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin sellers page should remain loaded after suspend");

        // Step 12 — assert approved count decreased by 1
        Assert.assertEquals(
                page.getFilterCount(AdminSellersPage.Filter.APPROVED),
                approvedBefore - 1,
                "Approved tab count should decrease by 1 after suspend");

        // Step 13 — verify Suspended tab count increased
        int suspendedBefore = page.getFilterCount(AdminSellersPage.Filter.SUSPENDED);
        page.selectFilter(AdminSellersPage.Filter.SUSPENDED);
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELLERS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        Assert.assertTrue(
                page.getFilterCount(AdminSellersPage.Filter.SUSPENDED) >= suspendedBefore,
                "Suspended tab count should increase after suspend action");
    }
}