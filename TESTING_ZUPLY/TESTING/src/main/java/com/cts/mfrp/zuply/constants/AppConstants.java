package com.cts.mfrp.zuply.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final String CONTENT_TYPE_JSON      = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    public static final String AUTH_HEADER  = "Authorization";
    public static final String BEARER       = "Bearer ";

    public static final String ROLE_BUYER   = "BUYER";
    public static final String ROLE_SELLER  = "SELLER";
    public static final String ROLE_ADMIN   = "ADMIN";

    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_APPROVED   = "APPROVED";
    public static final String STATUS_REJECTED   = "REJECTED";
    public static final String STATUS_SUSPENDED  = "SUSPENDED";
    public static final String STATUS_DRAFT      = "DRAFT";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED    = "SHIPPED";
    public static final String STATUS_DELIVERED  = "DELIVERED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_PAID       = "PAID";

    public static final String TESTDATA_DIR = "src/test/resources/testdata/";
}
