package com.cts.mfrp.zuply.utils;

import com.cts.mfrp.zuply.utils.ConfigReader;
import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

/**
 * Resolves a JWT for each role exactly once per test run.
 *
 *  - admin: logs in with the pre-existing admin@zuply.in credentials from config.
 *  - buyer/seller: when *.bootstrap=true (default), registers a fresh user with
 *    a UUID-suffixed email then logs in. Falls back to "CUSTOMER" if the API
 *    rejects role=BUYER.
 *
 * The resolved (email, password, token) tuple is cached for the duration of the JVM.
 */
public final class AuthManager {

    private static final Map<String, Credentials> credCache = new ConcurrentHashMap<>();

    private AuthManager() {}

    public static String buyerToken()  { return tokenFor("buyer");  }
    public static String sellerToken() { return tokenFor("seller"); }
    public static String adminToken()  { return tokenFor("admin");  }

    /** Login as an arbitrary user — does NOT cache. Use for negative-auth tests. */
    public static Response login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email",    email);
        body.put("password", password);
        return baseRequest().body(body).post(Endpoints.AUTH_LOGIN);
    }

    /** Register a new user. Returns the raw response so callers can branch on status. */
    public static Response register(String name, String email, String password, String role) {
        return register(name, email, password, role, null);
    }

    public static Response register(String name, String email, String password, String role, String phone) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        if (phone != null && !phone.isBlank()) body.put("phone", phone);
        return baseRequest().body(body).post(Endpoints.AUTH_REGISTER);
    }

    public static void invalidate(String role) { credCache.remove(role); }
    public static void invalidateAll()          { credCache.clear(); }

    /* ------------------------------------------------------------------ */

    private static String tokenFor(String role) {
        return credCache.computeIfAbsent(role, AuthManager::resolve).token;
    }

    private static Credentials resolve(String role) {
        if ("admin".equals(role)) {
            return staticLogin(role);
        }
        boolean bootstrap;
        try { bootstrap = ConfigReader.getBoolean(role + ".bootstrap"); }
        catch (Exception e) { bootstrap = true; }

        return bootstrap ? bootstrapAccount(role) : staticLogin(role);
    }

    private static Credentials staticLogin(String role) {
        String email    = ConfigReader.get(role + ".email");
        String password = ConfigReader.get(role + ".password");
        Response r = login(email, password);
        if (r.statusCode() != 200) {
            throw new RuntimeException(
                "Login failed for role=" + role + " status=" + r.statusCode()
                + " body=" + r.asString());
        }
        return new Credentials(email, password, extractToken(r, role));
    }

    private static Credentials bootstrapAccount(String role) {
        String password = ConfigReader.get(role + ".password");
        String name     = ConfigReader.get(role + ".name");
        String preferredRole = ConfigReader.get(role + ".role");
        String phone;
        try { phone = ConfigReader.get(role + ".phone"); }
        catch (Exception e) { phone = null; }
        String email    = "auto." + role + "." + shortId() + "@zuply.in";

        Response reg = register(name, email, password, preferredRole, phone);
        if (reg.statusCode() != 201 && reg.statusCode() != 200) {
            throw new RuntimeException(
                "Bootstrap registration failed for role=" + role + " status=" + reg.statusCode()
                + " body=" + reg.asString());
        }
        Response loginResp = login(email, password);
        if (loginResp.statusCode() != 200) {
            throw new RuntimeException(
                "Bootstrap login failed for role=" + role
                + " (registered " + email + ") status=" + loginResp.statusCode()
                + " body=" + loginResp.asString());
        }
        return new Credentials(email, password, extractToken(loginResp, role));
    }

    private static String extractToken(Response r, String role) {
        String token = ResponseUtils.body(r).getString("token");
        if (token == null || token.isBlank()) token = r.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                "Login OK but no token in response for role=" + role + " body=" + r.asString());
        }
        return token;
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private static io.restassured.specification.RequestSpecification baseRequest() {
        return given()
                .baseUri(ConfigReader.get("base.url"))
                .contentType(ContentType.JSON);
    }

    private record Credentials(String email, String password, String token) {}
}
