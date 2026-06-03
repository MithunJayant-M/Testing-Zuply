package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AdminClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response dashboard(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ADMIN_DASHBOARD);
    }

    public Response dashboardNoAuth() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.ADMIN_DASHBOARD);
    }

    public Response getSellers(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ADMIN_SELLERS);
    }

    public Response approveSeller(String token, Object sellerId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", sellerId)
               .when()
                    .patch(Endpoints.ADMIN_SELLER_APPROVE);
    }

    public Response suspendSeller(String token, Object sellerId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", sellerId)
               .when()
                    .patch(Endpoints.ADMIN_SELLER_SUSPEND);
    }

    public Response deleteSeller(String token, Object sellerId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", sellerId)
               .when()
                    .delete(Endpoints.ADMIN_SELLER_BY_ID);
    }

    public Response pendingProducts(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ADMIN_PRODUCTS);
    }

    public Response approveProduct(String token, Object productId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", productId)
               .when()
                    .patch(Endpoints.ADMIN_PRODUCT_APPROVE);
    }

    public Response rejectProduct(String token, Object productId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", productId)
                    .body(body)
               .when()
                    .patch(Endpoints.ADMIN_PRODUCT_REJECT);
    }

    public Response updateProduct(String token, Object productId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", productId)
                    .body(body)
               .when()
                    .put(Endpoints.ADMIN_PRODUCT_BY_ID);
    }

    public Response deleteProduct(String token, Object productId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("id", productId)
               .when()
                    .delete(Endpoints.ADMIN_PRODUCT_BY_ID);
    }

    public Response getOrders(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ADMIN_ORDERS);
    }

    public Response getReports(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.ADMIN_REPORTS);
    }
}
