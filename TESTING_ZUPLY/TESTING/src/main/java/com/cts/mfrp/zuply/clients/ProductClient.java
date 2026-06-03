package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ProductClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response search(Map<String, Object> queryParams) {
        return given()
                    .spec(RequestBuilder.spec())
                    .queryParams(queryParams)
               .when()
                    .get(Endpoints.PRODUCTS);
    }

    public Response searchAll() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.PRODUCTS);
    }

    public Response getById(Object id) {
        return given()
                    .spec(RequestBuilder.spec())
                    .pathParam("id", id)
               .when()
                    .get(Endpoints.PRODUCT_BY_ID);
    }

    public Response create(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.PRODUCTS);
    }

    public Response createNoAuth(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .post(Endpoints.PRODUCTS);
    }

    public Response update(String token, Object id, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", id)
                    .body(body)
               .when()
                    .put(Endpoints.PRODUCT_BY_ID);
    }

    public Response delete(String token, Object id) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", id)
               .when()
                    .delete(Endpoints.PRODUCT_BY_ID);
    }

    public Response getBySeller(Object sellerId) {
        return given()
                    .spec(RequestBuilder.spec())
                    .pathParam("sellerId", sellerId)
               .when()
                    .get(Endpoints.PRODUCTS_BY_SELLER);
    }
}
