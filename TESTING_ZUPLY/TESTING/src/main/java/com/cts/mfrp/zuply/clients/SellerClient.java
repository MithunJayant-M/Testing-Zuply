package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class SellerClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response register(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.SELLER_REGISTER);
    }

    public Response registerNoAuth(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .post(Endpoints.SELLER_REGISTER);
    }

    public Response dashboard(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.SELLER_DASHBOARD);
    }

    public Response dashboardNoAuth() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.SELLER_DASHBOARD);
    }

    public Response sellerProducts(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.SELLER_PRODUCTS);
    }

    public Response sellerOrders(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.SELLER_ORDERS);
    }

    public Response updateOrderStatus(String token, Object orderId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", orderId)
                    .queryParams(body)
               .when()
                    .patch(Endpoints.SELLER_ORDER_STATUS);
    }

    public Response getPublicProfile(Object sellerId) {
        return given()
                    .spec(RequestBuilder.spec())
                    .pathParam("id", sellerId)
               .when()
                    .get(Endpoints.SELLER_PUBLIC);
    }
}
