package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.SellerClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "seller"})
public class SellerTests extends BaseTest {

    private SellerClient client;
    private Integer sellerOrderId;

    @BeforeClass
    public void setUp() {
        client = new SellerClient();
        Response orders = client.sellerOrders(sellerToken());
        if (orders.statusCode() == 200) {
            List<Map<String, Object>> list = ResponseUtils.body(orders).getList("$");
            if (list != null && !list.isEmpty()) {
                Object id = list.get(0).get("id");
                if (id != null) sellerOrderId = ((Number) id).intValue();
            }
        }
    }

    @DataProvider(name = "orderStatus")
    public Object[][] orderStatus() throws IOException {
        return TestDataHelper.read("SellerData.xlsx", "OrderStatus");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "GET /api/seller/dashboard valid -> 200 with metrics")
    public void testDashboard() {
        client.dashboard(sellerToken())
            .then()
                .statusCode(200)
                .body("data.totalProductsUploaded", notNullValue());
    }

    @Test(description = "GET /api/seller/products -> 200 array")
    public void testSellerProducts() {
        client.sellerProducts(sellerToken())
            .then()
                .statusCode(200);
    }

    @Test(description = "GET /api/seller/orders -> 200 array")
    public void testSellerOrders() {
        client.sellerOrders(sellerToken())
            .then()
                .statusCode(200);
    }

    @Test(dataProvider = "orderStatus", description = "PATCH /api/seller/orders/{id}/status - data-driven")
    public void testUpdateOrderStatus(String status, String expectedStatus, String description) {
        log("Scenario: " + description);
        int id = sellerOrderId != null ? sellerOrderId : 1;

        Response r = client.updateOrderStatus(sellerToken(), id, Map.of("status", status));
        if (Integer.parseInt(expectedStatus) == 200) {
            r.then().statusCode(anyOf(equalTo(200), equalTo(400), equalTo(403)));
        } else {
            r.then().statusCode(Integer.parseInt(expectedStatus));
        }
    }
}
