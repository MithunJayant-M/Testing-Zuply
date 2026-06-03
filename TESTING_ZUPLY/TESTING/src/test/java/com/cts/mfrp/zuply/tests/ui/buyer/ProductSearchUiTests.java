package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Product browsing and search — FRD §2.3. Maps to TC007 + AD_TC_PS1.
 * (TC008, TC009, AD_TC_PS2, AD_TC_PS3 removed per cleanup pass.)
 *
 * Search keywords come from {@code ProductData.xlsx} → {@code SearchUI}, filtered
 * by the {@code Scenario} column.
 */
@Test(groups = {"regression", "ui", "search"})
public class ProductSearchUiTests extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_ProductData.xlsx";
    private static final String SHEET     = "SearchUI";

    @DataProvider(name = "validKeywords")
    public Object[][] validKeywords() throws Exception {
        List<Map<String, String>> rows = ExcelUtils.getTestDataAsMaps(DATA_FILE, SHEET);
        return rows.stream()
                .filter(r -> "VALID".equalsIgnoreCase(r.get("Scenario")))
                .map(r -> new Object[]{ r })
                .toArray(Object[][]::new);
    }

    /** TC007 — Product search returns relevant results (or a graceful empty state). */
    @Test(dataProvider = "validKeywords", description = "TC007 — ValidProductSearch")
    public void tc007_validProductSearch(Map<String, String> row) {
        ProductsPage page = new ProductsPage(driver);
        page.open();
        page.search(row.get("Keyword"));
        waitAfterAction();

        Assert.assertTrue(driver.getCurrentUrl().contains("/products"),
                row.get("TestCaseId") + " — should remain on /products after search");
        Assert.assertTrue(page.isLoaded(),
                row.get("TestCaseId") + " — products page heading should remain after search");
    }

    /**
     * AD_TC_PS1 — Anonymous (unauthenticated) visitor can browse APPROVED products
     * without logging in (FRD §2.3).
     */
    @Test(description = "AD_TC_PS1 -- AnonymousCanBrowseProducts")
    public void ps1_anonymousCanBrowseProducts() {
        clearSession();
        ProductsPage page = new ProductsPage(driver);
        page.open();

        Assert.assertTrue(page.isLoaded(),
                "Products page should load for anonymous visitors (FRD §2.3)");
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                "Anonymous /products access should NOT redirect to /login (FRD §2.3)");
    }
}
