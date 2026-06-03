package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.OrdersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Order management — FRD §2.6. Maps to TC017.
 * (TC018 — OrderStatusUpdate removed per cleanup pass; it covered seller flow and
 * belongs to the seller suite, not the buyer suite.)
 */
@Test(groups = {"regression", "ui", "orders"})
public class OrderTrackingUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void seedBuyer() {
        buyerEmail = registerNewCustomer("OrdersUser");
    }

    /** TC017 — Customer can view their order history. */
    @Test(description = "TC017 — OrderHistory")
    public void tc017_orderHistory() {
        loginViaUi(buyerEmail, defaultPassword());

        OrdersPage page = new OrdersPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Orders page heading should be visible");
        Assert.assertTrue(page.orderCount() >= 0,
                "Orders count should be a non-negative number");
    }
}
