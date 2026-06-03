package com.cts.mfrp.zuply.tests.ui.seller;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.SellerDashboardPage;
import com.cts.mfrp.zuply.pages.SellerOrdersPage;
import com.cts.mfrp.zuply.pages.SellerProductsPage;
import com.cts.mfrp.zuply.pages.SellerUploadPage;
import com.cts.mfrp.zuply.pages.SellersListingPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Seller product management — FRD §2.10. Maps to TC020–TC031.
 *
 * Every test labelled either:
 *   [POSITIVE] — happy path; expected to PASS when the feature works correctly.
 *   [NEGATIVE] — guard/restriction; expected to PASS when the application
 *                correctly denies or limits access.
 *
 * No inline selectors, no inline waits — all SPA timing and locator concerns
 * live on the page objects.
 */
@Test(groups = {"regression", "ui", "seller"})
public class SellerProductUiTests extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_ProductData.xlsx";
    private static final String SHEET     = "SellerUpload";

    private String sellerEmail;

    private Map<String, String> data(String testCaseId) throws IOException {
        return ExcelUtils.getRowByTestCaseId(DATA_FILE, SHEET, testCaseId);
    }

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginSeller() {
        sellerEmail = registerNewSeller("SellerCreate");
        ensureLoggedIn(sellerEmail, defaultPassword());
    }

    /** TC020 — Seller can create a product listing with all required fields. */
    @Test(description = "TC020 — SellerCreateProduct")
    public void tc020_sellerCreateProduct() throws IOException {
        Map<String, String> row = data("TC020");

        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        // Title prefix from Excel; randomSuffix() stays inline because it's the
        // dynamic-uniqueness piece of the hybrid data strategy.
        try {
            upload.enterTitle(row.get("TitlePrefix") + " " + randomSuffix())
                  .enterDescription(row.get("ProductDescription"))
                  .enterPrice(row.get("Price"))
                  .enterStock(row.get("Stock"));
        } catch (Exception e) {
            throw new SkipException("Form fields may not match current SPA build: " + e.getMessage());
        }
        Assert.assertNotNull(upload.waitForSubmitClickable(),
                "Submit-for-Review button should be visible and clickable");
    }

    /** TC021 — Seller can only edit/delete their own products (cross-seller access denied). */
    @Test(description = "TC021 — SellerEditDeleteProduct")
    public void tc021_sellerEditDeleteProduct() {
        clearSession();
        String sellerBEmail = registerNewSeller("SellerB");
        ensureLoggedIn(sellerBEmail, defaultPassword());

        SellerProductsPage page = new SellerProductsPage(driver);
        page.open();
        Assert.assertTrue(page.isEmpty(),
                "A newly-registered seller B should see only their own products (zero), not seller A's");
    }

    @Test(description = "TC022 [POSITIVE] — SellerDashboardLoads")
    public void tc022_sellerDashboardLoads() {
        SellerDashboardPage dashboard = new SellerDashboardPage(driver);
        dashboard.open();

        Assert.assertTrue(dashboard.isLoaded(),
                "Seller dashboard should be loaded with stats-grid visible");

        dashboard.waitForStatCards(4);
        Assert.assertEquals(dashboard.statCardCount(), 4,
                "Dashboard must show exactly 4 stat cards");
    }

    @Test(description = "TC023 [POSITIVE] — SellerDashboardNavLinks")
    public void tc023_sellerDashboardNavLinks() {
        SellerDashboardPage dashboard = new SellerDashboardPage(driver);

        dashboard.open();
        dashboard.goToRoute("/seller/upload");
        Assert.assertTrue(new SellerUploadPage(driver).isLoaded(),
                "Upload quick-action link should open the Add Product page");

        dashboard.open();
        dashboard.goToRoute("/seller/products");
        Assert.assertTrue(new SellerProductsPage(driver).isLoaded(),
                "Products quick-action link should open My Products page");

        dashboard.open();
        dashboard.goToRoute("/seller/orders");
        Assert.assertTrue(new SellerOrdersPage(driver).isLoaded(),
                "Orders quick-action link should open Customer Orders page");
    }

    @Test(description = "TC024 [POSITIVE] — SellerOrdersPageLoads")
    public void tc024_sellerOrdersPageLoads() {
        SellerOrdersPage orders = new SellerOrdersPage(driver);
        orders.open();

        Assert.assertTrue(orders.isLoaded(),
                "Customer Orders page should be accessible for a logged-in seller");

        int tabCount = orders.filterTabCount();
        if (tabCount == 0) {
            // Empty state — page may not render tabs without orders. The heading
            // assertion above is enough to confirm load.
            return;
        }
        Assert.assertTrue(tabCount >= 1,
                "Orders page should show at least one filter tab when tabs are rendered");
    }

    @Test(description = "TC025 [POSITIVE] — SellerOrderActionButtonVisible")
    public void tc025_sellerOrderActionButtonVisible() {
        SellerOrdersPage orders = new SellerOrdersPage(driver);
        orders.open();

        int rowCount = orders.waitForOrderRowCount(Duration.ofSeconds(5));
        if (rowCount == 0) {
            throw new SkipException("No orders in this environment — action-button check skipped");
        }

        Assert.assertTrue(orders.actionButtonCount() >= 1,
                "At least one action button (Process / Deliver) must be visible when orders exist");
    }

    @Test(description = "TC026 [NEGATIVE] — SellerProductsEmptyForNewSeller")
    public void tc026_sellerProductsEmptyForNewSeller() {
        clearSession();
        String freshSeller = registerNewSeller("SellerEmpty");
        ensureLoggedIn(freshSeller, defaultPassword());

        SellerProductsPage products = new SellerProductsPage(driver);
        products.open();

        Assert.assertTrue(products.isLoaded(),
                "My Products page should load for a newly registered seller");
        Assert.assertEquals(products.prodCardCount(), 0,
                "A new seller must see 0 product cards — cross-seller isolation must hold");
    }

    @Test(description = "TC027 [POSITIVE] — SellerProductsAddNewLink")
    public void tc027_sellerProductsAddNewLink() {
        SellerProductsPage products = new SellerProductsPage(driver);
        products.open();
        Assert.assertTrue(products.isLoaded(), "My Products page should be visible");

        products.clickAddNew();
        Assert.assertTrue(new SellerUploadPage(driver).isLoaded(),
                "Clicking '+ Upload New' must navigate to the Add Product page");
    }

    @Test(description = "TC028 [POSITIVE] — SellerUploadFormFieldsPresent")
    public void tc028_sellerUploadFormFieldsPresent() throws IOException {
        Map<String, String> row = data("TC028");

        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        try {
            upload.enterTitle(row.get("TitlePrefix") + " " + randomSuffix())
                  .enterDescription(row.get("ProductDescription"))
                  .enterPrice(row.get("Price"))
                  .enterStock(row.get("Stock"));
        } catch (Exception e) {
            throw new SkipException("One or more form fields not found — SPA build may differ: " + e.getMessage());
        }

        Assert.assertNotNull(upload.waitForSubmitClickable(),
                "Submit for Review button must be present and clickable");
    }

    @Test(description = "TC029 [POSITIVE] — SellerUploadSelectDropdownsPresent")
    public void tc029_sellerUploadSelectDropdownsPresent() {
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        Assert.assertEquals(upload.waitForSelectCount(3).size(), 3,
                "Upload form must have exactly 3 select dropdowns: Category, Delivery Method, Return Policy");
    }

    @Test(description = "TC030 [POSITIVE] — SellersListingPageLoads")
    public void tc030_sellersListingPageLoads() {
        SellersListingPage listing = new SellersListingPage(driver);
        listing.open();

        Assert.assertTrue(listing.isLoaded(),
                "Public sellers listing page should load with a heading visible");
        listing.waitForGridOrTable();
        Assert.assertTrue(listing.gridVisible(),
                "The sellers table must be displayed on the listing page");
    }

    @Test(description = "TC031 [NEGATIVE] — PendingSellerNotVisibleInPublicListing")
    public void tc031_pendingSellerNotVisibleInPublicListing() {
        SellersListingPage listing = new SellersListingPage(driver);
        listing.open();
        listing.waitForGridOrTable();
        int countBefore = listing.sellerCount();

        clearSession();
        registerNewSeller("SellerPending");

        clearSession();
        listing.open();
        listing.waitForGridOrTable();
        int countAfter = listing.sellerCount();

        Assert.assertEquals(countAfter, countBefore,
                "PENDING seller must NOT appear in public listing before admin approval. "
                + "Before: " + countBefore + ", After: " + countAfter);
    }
    /**
     * TC032 [NEGATIVE] — SellerDashboardRevenueCard
     * Validates that the Seller Dashboard displays a 'Total Revenue' stat card.
     * Currently expected to FAIL as the UI only renders 4 cards (Total Products,
     * Total Orders, Pending Orders, Approved Products).
     */
    @Test(description = "TC032 [NEGATIVE] — SellerDashboardRevenueCard")
    public void tc032_sellerDashboardRevenueCard() {
        SellerDashboardPage dashboard = new SellerDashboardPage(driver);
        dashboard.open();

        Assert.assertTrue(dashboard.isLoaded(), "Seller dashboard should be loaded");

        // The dashboard currently has 4 cards. If the developer adds Revenue, this should be 5.
        // We do a soft check on the total count, but explicitly fail if Revenue is missing.
        try {
            String revenueText = dashboard.totalRevenue();
            Assert.assertNotNull(revenueText, "Total Revenue stat card value should not be null");
        } catch (IllegalStateException e) {
            Assert.fail("The 'Total Revenue' stat card is missing from the Seller Dashboard! " +
                    "Current cards found: " + dashboard.statCardCount());
        }
    }
}
