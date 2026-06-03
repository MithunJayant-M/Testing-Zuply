package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response register(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .post(Endpoints.AUTH_REGISTER);
    }

    public Response login(Map<String, Object> body) {
        return given()
                    .spec(RequestBuilder.spec())
                    .body(body)
               .when()
                    .post(Endpoints.AUTH_LOGIN);
    }
}
