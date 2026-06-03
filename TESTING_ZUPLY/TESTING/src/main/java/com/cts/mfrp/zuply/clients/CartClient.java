package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class CartClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response getCart(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.CART);
    }

    public Response getCartNoAuth() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.CART);
    }

    public Response addItem(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.CART);
    }

    public Response updateItem(String token, Object itemId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("itemId", itemId)
                    .queryParams(body)
               .when()
                    .put(Endpoints.CART_ITEM);
    }

    public Response deleteItem(String token, Object itemId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("itemId", itemId)
               .when()
                    .delete(Endpoints.CART_ITEM);
    }
}
