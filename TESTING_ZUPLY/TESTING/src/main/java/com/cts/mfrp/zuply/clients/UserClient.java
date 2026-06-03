package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response getProfile(String token) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
               .when()
                    .get(Endpoints.USERS_PROFILE);
    }

    public Response getProfileNoAuth() {
        return given()
                    .spec(RequestBuilder.spec())
               .when()
                    .get(Endpoints.USERS_PROFILE);
    }

    public Response updateProfile(String token, Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.authSpec(token))
                    .body(body)
               .when()
                    .put(Endpoints.USERS_PROFILE);
    }

    public Response updateProfileNoAuth(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .put(Endpoints.USERS_PROFILE);
    }
}
