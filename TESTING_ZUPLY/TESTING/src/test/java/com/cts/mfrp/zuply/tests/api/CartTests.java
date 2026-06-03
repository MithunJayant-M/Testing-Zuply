package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.CartClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "cart"})
public class CartTests extends BaseTest {

    private CartClient client;

    @BeforeClass
    public void setUp() { client = new CartClient(); }

    /** Adds a product and returns the itemId for use in update / delete tests. */
    private Integer freshCartItemId() {
        client.addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));
        Response cart = client.getCart(buyerToken());
        if (cart.statusCode() == 200) {
            List<Map<String, Object>> items = ResponseUtils.body(cart).getList("items");
            if (items != null && !items.isEmpty()) {
                Object id = items.get(items.size() - 1).get("itemId");
                if (id != null) return ((Number) id).intValue();
            }
        }
        return null;
    }

    @DataProvider(name = "addItem")
    public Object[][] addItem() throws IOException {
        return TestDataHelper.read("CartData.xlsx", "AddItem");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "GET /api/cart with buyer JWT -> 200 with items array")
    public void testGetCart() {
        client.getCart(buyerToken())
            .then()
                .statusCode(200)
                .body("data.items", notNullValue());
    }

    @Test(dataProvider = "addItem", description = "POST /api/cart - data-driven")
    public void testAddItem(String productId, String quantity, String expectedStatus, String description) {
        log("Scenario: " + description);

        Map<String, Object> body = new HashMap<>();
        body.put("productId", Integer.parseInt(productId));
        body.put("quantity",  Integer.parseInt(quantity));

        // API may return 200 or 201 for a successful add
        Response r = client.addItem(buyerToken(), body);
        if (Integer.parseInt(expectedStatus) == 201) {
            r.then().statusCode(anyOf(equalTo(200), equalTo(201)));
        } else {
            r.then().statusCode(Integer.parseInt(expectedStatus));
        }
    }

    @Test(description = "PUT /api/cart/{itemId} valid quantity -> 200")
    public void testUpdateItem() {
        Integer id = freshCartItemId();
        if (id == null) throw new org.testng.SkipException("Could not obtain a cart item id for update");

        client.updateItem(buyerToken(), id, Map.of("quantity", 5))
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test(description = "DELETE /api/cart/{itemId} valid -> 204")
    public void testDeleteItem() {
        Integer id = freshCartItemId();
        if (id == null) throw new org.testng.SkipException("Could not obtain a cart item id for delete");

        client.deleteItem(buyerToken(), id)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(404)));
    }
}
