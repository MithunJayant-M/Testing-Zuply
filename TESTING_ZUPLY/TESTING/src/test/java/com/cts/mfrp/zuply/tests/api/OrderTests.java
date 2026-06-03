package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.CartClient;
import com.cts.mfrp.zuply.clients.OrderClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "orders"})
public class OrderTests extends BaseTest {

    private OrderClient orderClient;

    @BeforeClass
    public void setUp() {
        orderClient = new OrderClient();
        new CartClient().addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));
    }

    @DataProvider(name = "place")
    public Object[][] place() throws IOException {
        return TestDataHelper.read("OrderData.xlsx", "Place");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(dataProvider = "place", description = "POST /api/orders - data-driven")
    public void testPlaceOrder(String address, String city, String pincode, String paymentMethod,
                               String expectedStatus, String description) {
        log("Scenario: " + description);

        Map<String, Object> deliveryAddress = new HashMap<>();
        deliveryAddress.put("customerName", "Test Buyer");
        deliveryAddress.put("phone",        "9876543210");
        if (!address.isBlank()) deliveryAddress.put("address", address);
        deliveryAddress.put("city",    city);
        deliveryAddress.put("pincode", pincode);

        Map<String, Object> body = new HashMap<>();
        body.put("deliveryAddress", deliveryAddress);
        body.put("paymentMethod",   paymentMethod);

        orderClient.placeOrder(buyerToken(), body)
            .then()
                .statusCode(Integer.parseInt(expectedStatus));
    }

    @Test(description = "GET /api/orders -> 200 with orders array")
    public void testGetOrders() {
        orderClient.getOrders(buyerToken())
            .then()
                .statusCode(200)
                .body("data", notNullValue());
    }

    @Test(description = "GET /api/orders/{id} valid -> 200 or 404")
    public void testGetOrderById() {
        orderClient.getOrderById(buyerToken(), 1)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }
}
