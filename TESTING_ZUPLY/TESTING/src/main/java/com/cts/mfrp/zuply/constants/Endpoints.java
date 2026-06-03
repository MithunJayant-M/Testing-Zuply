package com.cts.mfrp.zuply.constants;

public final class Endpoints {

    private Endpoints() {}

    // Auth
    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGIN    = "/api/auth/login";

    // Users
    public static final String USERS_PROFILE = "/api/users/profile";

    // Products
    public static final String PRODUCTS              = "/api/products";
    public static final String PRODUCT_BY_ID         = "/api/products/{id}";
    public static final String PRODUCTS_BY_SELLER    = "/api/products/seller/{sellerId}";
    public static final String PRODUCT_REVIEWS       = "/api/products/{productId}/reviews";

    // Categories
    public static final String CATEGORIES = "/api/categories";

    // Upload
    public static final String UPLOAD = "/api/upload";

    // Listing
    public static final String LISTING_GENERATE  = "/api/listing/generate/{imageId}";
    public static final String LISTING_BY_IMAGE  = "/api/listing/{imageId}";
    public static final String LISTING_BY_PRODUCT= "/api/listing/{productId}";
    public static final String LISTING_PUBLISH   = "/api/listing/{productId}/publish";
    public static final String LISTING_IMAGES    = "/api/listing/{productId}/images";

    // Cart
    public static final String CART          = "/api/cart";
    public static final String CART_ITEM     = "/api/cart/{itemId}";

    // Wishlist
    public static final String WISHLIST              = "/api/wishlist";
    public static final String WISHLIST_BY_PRODUCT   = "/api/wishlist/{productId}";

    // Orders
    public static final String ORDERS        = "/api/orders";
    public static final String ORDER_BY_ID   = "/api/orders/{id}";

    // Payment
    public static final String PAYMENT_CREATE_ORDER = "/api/payment/create-order";
    public static final String PAYMENT_VERIFY       = "/api/payment/verify";
    public static final String PAYMENT_STATUS       = "/api/payment/status/{orderId}";

    // Seller
    public static final String SELLER_REGISTER    = "/api/seller/register";
    public static final String SELLER_DASHBOARD   = "/api/seller/dashboard";
    public static final String SELLER_PRODUCTS    = "/api/seller/products";
    public static final String SELLER_ORDERS      = "/api/seller/orders";
    public static final String SELLER_ORDER_STATUS= "/api/seller/orders/{id}/status";
    public static final String SELLER_PUBLIC      = "/api/seller/{id}";

    // Admin
    public static final String ADMIN_DASHBOARD       = "/api/admin/dashboard";
    public static final String ADMIN_SELLERS         = "/api/admin/sellers";
    public static final String ADMIN_SELLER_APPROVE  = "/api/admin/sellers/{id}/approve";
    public static final String ADMIN_SELLER_SUSPEND  = "/api/admin/sellers/{id}/suspend";
    public static final String ADMIN_SELLER_BY_ID    = "/api/admin/sellers/{id}";
    public static final String ADMIN_PRODUCTS        = "/api/admin/products";
    public static final String ADMIN_PRODUCT_APPROVE = "/api/admin/products/{id}/approve";
    public static final String ADMIN_PRODUCT_REJECT  = "/api/admin/products/{id}/reject";
    public static final String ADMIN_PRODUCT_BY_ID   = "/api/admin/products/{id}";
    public static final String ADMIN_ORDERS          = "/api/admin/orders";
    public static final String ADMIN_REPORTS         = "/api/admin/reports";
}
