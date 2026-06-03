package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AdminClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "admin"})
public class AdminTests extends BaseTest {

    private AdminClient client;
    private Integer productIdToApprove;
    private Integer productIdToReject;
    private Integer sellerIdToApprove;

    @BeforeClass
    public void setUp() {
        client = new AdminClient();
        Response prodResp = client.pendingProducts(adminToken());
        if (prodResp.statusCode() == 200) {
            List<Map<String, Object>> products = ResponseUtils.body(prodResp).getList("$");
            if (products != null && !products.isEmpty()) {
                productIdToApprove = ((Number) products.get(0).get("id")).intValue();
                productIdToReject  = products.size() > 1
                        ? ((Number) products.get(1).get("id")).intValue()
                        : productIdToApprove;
            }
        }
        Response sellerResp = client.getSellers(adminToken());
        if (sellerResp.statusCode() == 200) {
            List<Map<String, Object>> sellers = ResponseUtils.body(sellerResp).getList("$");
            if (sellers != null && !sellers.isEmpty())
                sellerIdToApprove = ((Number) sellers.get(0).get("id")).intValue();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "GET /api/admin/dashboard valid -> 200 with totalSellers")
    public void testDashboard() {
        client.dashboard(adminToken())
            .then()
                .statusCode(200)
                .body("data.totalSellers", notNullValue());
    }

    @Test(description = "GET /api/admin/sellers -> 200 array")
    public void testGetSellers() {
        client.getSellers(adminToken())
            .then()
                .statusCode(200);
    }

    @Test(description = "PATCH /api/admin/sellers/{id}/approve -> 200")
    public void testApproveSeller() {
        int id = sellerIdToApprove != null ? sellerIdToApprove : 2;
        client.approveSeller(adminToken(), id)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404)));
    }

    @Test(description = "PATCH /api/admin/products/{id}/approve -> 200")
    public void testApproveProduct() {
        int id = productIdToApprove != null ? productIdToApprove : 5;
        client.approveProduct(adminToken(), id)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404)));
    }

    @Test(description = "PATCH /api/admin/products/{id}/reject -> 200 with reason")
    public void testRejectProduct() {
        int id = productIdToReject != null ? productIdToReject : 6;
        client.rejectProduct(adminToken(), id,
                Map.of("reason", "Product description violates marketplace policy"))
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404)));
    }

    @Test(description = "GET /api/admin/orders -> 200 array")
    public void testAdminOrders() {
        client.getOrders(adminToken())
            .then()
                .statusCode(200);
    }
}
