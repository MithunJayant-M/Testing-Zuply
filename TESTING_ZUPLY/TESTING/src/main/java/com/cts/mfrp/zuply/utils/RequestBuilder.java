package com.cts.mfrp.zuply.utils;

import com.cts.mfrp.zuply.constants.AppConstants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Centralized RequestSpecification factory.
 * Use {@link #spec()} for unauthenticated calls and {@link #authSpec(String)}
 * to attach a Bearer token.
 */
public final class RequestBuilder {

    private RequestBuilder() {}

    public static RequestSpecification spec() {
        RequestSpecBuilder b = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.url"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);
        if (ConfigReader.getBoolean("log.requests")) {
            b.addFilter(new RequestLoggingFilter())
             .addFilter(new ResponseLoggingFilter());
        }
        return io.restassured.RestAssured.given().spec(b.build());
    }

    public static RequestSpecification authSpec(String token) {
        return spec().header(AppConstants.AUTH_HEADER, AppConstants.BEARER + token);
    }

    public static RequestSpecification multipartSpec(String token) {
        RequestSpecBuilder b = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.url"))
                .setContentType(ContentType.MULTIPART);
        if (ConfigReader.getBoolean("log.requests")) {
            b.addFilter(new RequestLoggingFilter())
             .addFilter(new ResponseLoggingFilter());
        }
        return io.restassured.RestAssured.given()
                .spec(b.build())
                .header(AppConstants.AUTH_HEADER, AppConstants.BEARER + token);
    }
}
