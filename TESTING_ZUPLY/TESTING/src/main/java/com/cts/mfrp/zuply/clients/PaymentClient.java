package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class PaymentClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response createOrder(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.PAYMENT_CREATE_ORDER);
    }

    public Response verify(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .post(Endpoints.PAYMENT_VERIFY);
    }

    public Response status(String token, Object orderId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("orderId", orderId)
               .when()
                    .get(Endpoints.PAYMENT_STATUS);
    }
}
