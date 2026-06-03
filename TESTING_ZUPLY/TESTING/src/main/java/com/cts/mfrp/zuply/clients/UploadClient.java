package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.io.File;

import static io.restassured.RestAssured.given;

public class UploadClient {

    // ── GIVEN: build request  WHEN: fire HTTP call ───────────────────────────

    public Response uploadFile(String token, File file) {
        return given()
                    .spec(RequestBuilder.multipartSpec(token))
                    .multiPart("file", file, "image/jpeg")
               .when()
                    .post(Endpoints.UPLOAD);
    }

    public Response uploadFileNoAuth(File file) {
        return given()
                    .spec(RequestBuilder.spec())
                    .multiPart("file", file, "image/jpeg")
               .when()
                    .post(Endpoints.UPLOAD);
    }

    public Response uploadEmpty(String token) {
        return given()
                    .spec(RequestBuilder.multipartSpec(token))
               .when()
                    .post(Endpoints.UPLOAD);
    }
}
