package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AdminClient;
import com.cts.mfrp.zuply.clients.ProductClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "products"})
public class ProductTests extends BaseTest {

    private ProductClient client;
    private Integer createdProductId;

    @BeforeClass
    public void setUp() {
        client = new ProductClient();
        AdminClient adminClient = new AdminClient();

        Response sellersResp = adminClient.getSellers(adminToken());
        if (sellersResp.statusCode() == 200) {
            List<Map<String, Object>> sellers = ResponseUtils.body(sellersResp).getList("$");
            if (sellers != null) {
                for (Map<String, Object> s : sellers) {
                    if ("PENDING".equals(s.get("verificationStatus"))) {
                        Object sid = s.get("id");
                        if (sid != null) adminClient.approveSeller(adminToken(), sid);
                    }
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name",        "ProductTests Fixture " + System.currentTimeMillis());
        body.put("description", "fixture product for read/update tests");
        body.put("categoryId",  5);
        body.put("price",       1999);
        body.put("stock",       10);
        Response r = client.create(sellerToken(), body);
        if (r.statusCode() == 201 || r.statusCode() == 200) {
            Object id = ResponseUtils.body(r).get("id");
            if (id == null) id = ResponseUtils.body(r).get("productId");
            if (id != null) createdProductId = ((Number) id).intValue();
        }
    }

    @DataProvider(name = "createProduct")
    public Object[][] createProduct() throws IOException {
        return TestDataHelper.read("ProductData.xlsx", "Create");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "GET /api/products -> 200 + non-null array")
    public void testSearchAll() {
        client.searchAll()
            .then()
                .statusCode(200)
                .body("data", notNullValue());
    }

    @Test(description = "GET /api/products/{id} valid -> 200 with id and name")
    public void testGetProductById() {
        if (createdProductId == null)
            throw new org.testng.SkipException("Setup failed to create product fixture");

        client.getById(createdProductId)
            .then()
                .statusCode(200)
                .body("data.id",   equalTo(createdProductId))
                .body("data.name", notNullValue());
    }

    @Test(description = "GET /api/products/{id} non-existent -> 404")
    public void testGetProductByIdNotFound() {
        client.getById(99999)
            .then()
                .statusCode(404);
    }

    @Test(dataProvider = "createProduct", description = "POST /api/products - data-driven")
    public void testCreateProduct(String name, String desc, String categoryId, String price,
                                  String stock, String expectedStatus, String description_tc) {
        log("Scenario: " + description_tc);

        Map<String, Object> body = new LinkedHashMap<>();
        if (!name.isBlank()) body.put("name", name);
        body.put("description", desc);
        body.put("categoryId",  Integer.parseInt(categoryId));
        body.put("price",       Double.parseDouble(price));
        body.put("stock",       Integer.parseInt(stock));

        client.create(sellerToken(), body)
            .then()
                .statusCode(Integer.parseInt(expectedStatus));
    }

    @Test(description = "POST /api/products as buyer -> 403 Forbidden")
    public void testCreateProductAsBuyerForbidden() {
        Map<String, Object> body = Map.of(
                "name", "Buyer Attempt", "description", "x",
                "category", "Electronics", "price", 100, "stock", 1);

        client.create(buyerToken(), body)
            .then()
                .statusCode(403);
    }

    @Test(description = "PUT /api/products/{id} as owning seller -> 200")
    public void testUpdateProduct() {
        if (createdProductId == null)
            throw new org.testng.SkipException("Setup failed to create product fixture");

        Map<String, Object> body = Map.of(
                "name",        "ProductTests Fixture (Updated)",
                "price",       2499,
                "stock",       5,
                "description", "updated description");

        client.update(sellerToken(), createdProductId, body)
            .then()
                .statusCode(200);
    }

    @Test(description = "GET /api/products/seller/{id} -> 200 with array")
    public void testGetProductsBySeller() {
        client.getBySeller(1)
            .then()
                .statusCode(200)
                .body("data", notNullValue());
    }
}
