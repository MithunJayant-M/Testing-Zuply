package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response placeOrder(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.ORDERS);
    }

    public Response placeOrderNoAuth(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .post(Endpoints.ORDERS);
    }

    public Response getOrders(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ORDERS);
    }

    public Response getOrdersNoAuth() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.ORDERS);
    }

    public Response getOrderById(String token, Object id) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", id)
               .when()
                    .get(Endpoints.ORDER_BY_ID);
    }
}
