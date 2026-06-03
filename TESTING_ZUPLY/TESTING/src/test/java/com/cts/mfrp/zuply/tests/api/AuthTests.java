package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AuthClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Test(groups = {"smoke", "regression", "api", "auth"})
public class AuthTests extends BaseTest {

    private AuthClient client;

    @BeforeClass
    public void setUp() { client = new AuthClient(); }

    @DataProvider(name = "registerData")
    public Object[][] registerData() throws IOException {
        return TestDataHelper.read("AuthData.xlsx", "Register");
    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() throws IOException {
        return TestDataHelper.read("AuthData.xlsx", "Login");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(dataProvider = "registerData", description = "POST /api/auth/register - data-driven")
    public void testRegister(String name, String email, String password, String role,
                             String phone, String expectedStatus, String description) {
        log("Scenario: " + description);

        Map<String, Object> body = new HashMap<>();
        if (!name.isBlank())  body.put("name", name);
        body.put("email",    email);
        body.put("password", password);
        body.put("role",     role);
        if (phone != null && !phone.isBlank()) body.put("phone", phone);

        // THEN
        Response r = client.register(body);
        r.then().statusCode(Integer.parseInt(expectedStatus));

        if (r.statusCode() == 201) {
            r.then()
                .body("data.email",  equalTo(email))
                .body("data.role",   equalTo(role))
                .body("data.userId", notNullValue());
        }
    }

    @Test(dataProvider = "loginData", description = "POST /api/auth/login - data-driven")
    public void testLogin(String email, String password, String expectedStatus, String description) {
        log("Scenario: " + description);

        Map<String, Object> body = new HashMap<>();
        body.put("email",    email);
        body.put("password", password);

        // THEN
        Response r = client.login(body);
        r.then().statusCode(Integer.parseInt(expectedStatus));

        if (r.statusCode() == 200) {
            r.then()
                .body("data.token", notNullValue())
                .body("data.role",  notNullValue());
        }
    }

    @Test(description = "Bootstrapped buyer login produces a non-empty JWT")
    public void testLoginReturnsJwt() {
        // THEN
        String token = buyerToken();
        assertThat("JWT must not be null",         token, notNullValue());
        assertThat("JWT suspiciously short",        token.length(), greaterThan(20));
    }
}
