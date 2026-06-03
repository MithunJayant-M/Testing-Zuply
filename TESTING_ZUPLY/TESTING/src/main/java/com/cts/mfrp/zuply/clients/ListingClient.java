package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ListingClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response generate(String token, Object imageId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("imageId", imageId)
               .when()
                    .post(Endpoints.LISTING_GENERATE);
    }

    public Response generateNoAuth(Object imageId) {
        return given()
                    .spec(RequestBuilder.spec())
                    .pathParam("imageId", imageId)
               .when()
                    .post(Endpoints.LISTING_GENERATE);
    }

    public Response getDraft(String token, Object imageId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("imageId", imageId)
               .when()
                    .get(Endpoints.LISTING_BY_IMAGE);
    }

    public Response editDraft(String token, Object productId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("productId", productId)
                    .body(body)
               .when()
                    .put(Endpoints.LISTING_BY_PRODUCT);
    }

    public Response publish(String token, Object productId) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("productId", productId)
               .when()
                    .post(Endpoints.LISTING_PUBLISH);
    }

    public Response saveImages(String token, Object productId, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .pathParam("productId", productId)
                    .body(body)
               .when()
                    .put(Endpoints.LISTING_IMAGES);
    }
}
