package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.UserClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "users"})
public class UserTests extends BaseTest {

    private UserClient client;

    @BeforeClass
    public void setUp() { client = new UserClient(); }

    @DataProvider(name = "profileUpdate")
    public Object[][] profileUpdate() throws IOException {
        return TestDataHelper.read("UserData.xlsx", "ProfileUpdate");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "GET /api/users/profile with valid JWT -> 200 with email")
    public void testGetProfileValid() {
        client.getProfile(buyerToken())
            .then()
                .statusCode(200)
                .body("data.email", notNullValue());
    }

    @Test(dataProvider = "profileUpdate", description = "PUT /api/users/profile - data-driven")
    public void testUpdateProfile(String name, String email, String expectedStatus, String description) {
        log("Scenario: " + description);

        Map<String, Object> body = new HashMap<>();
        body.put("name",    name);
        body.put("email",   email);
        body.put("phone",   "9876543210");
        body.put("address", "42, Anna Nagar, Chennai");
        body.put("pincode", "600040");

        Response r = client.updateProfile(buyerToken(), body);
        r.then().statusCode(Integer.parseInt(expectedStatus));
    }
}
