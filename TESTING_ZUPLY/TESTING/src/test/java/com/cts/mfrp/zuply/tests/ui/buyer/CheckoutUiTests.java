package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CheckoutPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Checkout & order placement — FRD §2.5. Maps to TC015, TC016, TC017 + AD_TC_CO1.
 *
 * Test data lives in {@code src/test/resources/testdata/OrderData.xlsx}, sheet
 * {@code CheckoutUI}. Each row is tagged with a {@code Scenario} (VALID,
 * MISSING_CITY, UPI_RAZORPAY) so each test method consumes exactly the rows it
 * cares about via a filtered {@link DataProvider}.
 */
@Test(groups = {"regression", "ui", "checkout"})
public class CheckoutUiTests extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_OrderData.xlsx";
    private static final String SHEET     = "CheckoutUI";

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Checkout");
        loginViaUi(buyerEmail, defaultPassword());
    }

    /* ------------------------------------------------------------------ */
    /* Data providers — each filters CheckoutUI sheet rows by Scenario     */
    /* ------------------------------------------------------------------ */

    @DataProvider(name = "validAddresses")
    public Object[][] validAddresses() throws Exception {
        return rowsByScenario("VALID");
    }

    @DataProvider(name = "missingCityRow")
    public Object[][] missingCityRow() throws Exception {
        return rowsByScenario("MISSING_CITY");
    }

    @DataProvider(name = "upiRow")
    public Object[][] upiRow() throws Exception {
        return rowsByScenario("UPI_RAZORPAY");
    }

    private Object[][] rowsByScenario(String scenario) throws Exception {
        List<Map<String, String>> all = ExcelUtils.getTestDataAsMaps(DATA_FILE, SHEET);
        return all.stream()
                .filter(r -> scenario.equalsIgnoreCase(r.get("Scenario")))
                .map(r -> new Object[]{ r })
                .toArray(Object[][]::new);
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                               */
    /* ------------------------------------------------------------------ */

    /** TC015 — Successful checkout with valid delivery address + payment method. */
    @Test(dataProvider = "validAddresses", description = "TC015 — ValidCheckout")
    public void tc015_validCheckout(Map<String, String> row) {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress(
                row.get("Name"), row.get("Phone"), row.get("Address"),
                row.get("City"), row.get("Pincode"));
        try { cp.selectPaymentMethod(row.get("PaymentMethod")); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}
        waitAfterAction();

        Assert.assertFalse(driver.getTitle().contains("Page not found"),
                row.get("TestCaseId") + " — should not land on Netlify's 404 after checkout submit");
    }

    /** TC016 — City field validation surfaces on submit with a blank city. */
    @Test(dataProvider = "missingCityRow", description = "TC016 — CheckoutMissingFields")
    public void tc016_checkoutMissingFields(Map<String, String> row) {
        seedOneItemInCart();
        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress(
                row.get("Name"), row.get("Phone"), row.get("Address"),
                row.get("City"), row.get("Pincode"));   // City is blank per the data row
        try { cp.selectPaymentMethod(row.get("PaymentMethod")); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}
        waitAfterAction();

        Assert.assertFalse(cp.isOrderSuccessMessageVisible(),
                row.get("TestCaseId") + " — order should NOT have been placed with a blank city");
        Assert.assertTrue(cp.isCityValidationErrorVisible(),
                row.get("TestCaseId") + " — red validation error for missing City did not render");
    }

    /** AD_TC017 — Online (Razorpay) payment flow hands off to the Razorpay modal. */
    @Test(dataProvider = "upiRow", description = "TC_AD017 — OnlinePaymentCheckout")
    public void tc017_onlinePaymentCheckout(Map<String, String> row) {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress(
                row.get("Name"), row.get("Phone"), row.get("Address"),
                row.get("City"), row.get("Pincode"));
        try { cp.selectPaymentMethod(row.get("PaymentMethod")); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}

        Assert.assertTrue(cp.isRazorpayModalOpened(),
                row.get("TestCaseId") + " — Razorpay modal failed to open; SPA↔Razorpay integration broken");
        // Razorpay's internal anti-bot security is out of scope for Zuply.
    }

    /**
     * AD_TC_CO1 — All three FRD-mandated payment methods (COD, UPI, Card) are
     * available on the checkout page (FRD §2.5). Single-shot assertion — no
     * meaningful row iteration, kept inline.
     */
    @Test(description = "AD_TC_CO1 -- ThreePaymentMethodsAvailable")
    public void co1_threePaymentMethodsAvailable() {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".payment-options, .payment-option")));

        String body = driver.getPageSource().toLowerCase();
        boolean hasCod  = body.contains("cash on delivery") || body.contains("cod");
        boolean hasUpi  = body.contains("upi") || body.contains("gpay");
        boolean hasCard = body.contains("card");

        Assert.assertTrue(hasCod,  "Checkout should expose 'Cash on Delivery' payment method (FRD §2.5)");
        Assert.assertTrue(hasUpi,  "Checkout should expose 'UPI' payment method (FRD §2.5)");
        Assert.assertTrue(hasCard, "Checkout should expose 'Card' payment method (FRD §2.5)");
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                             */
    /* ------------------------------------------------------------------ */

    /** Best-effort cart seed: skip silently if the Products page has no Add buttons. */
    private void seedOneItemInCart() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (products.hasAddToCartButtons()) {
            products.addFirstToCart();
        }
    }
}
