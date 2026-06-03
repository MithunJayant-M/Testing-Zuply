package com.cts.mfrp.zuply.base;

import com.cts.mfrp.zuply.utils.ConfigReader;
import com.cts.mfrp.zuply.utils.ExtentManager;
import com.cts.mfrp.zuply.utils.AuthManager;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import org.testng.annotations.BeforeSuite;

/**
 * Common base for all API test classes. Initializes RestAssured, ExtentReports,
 * and exposes role-scoped JWTs to subclasses via the AuthManager.
 */
public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void initSuite() {
        RestAssured.baseURI = ConfigReader.get("base.url");
        RestAssured.urlEncodingEnabled = false;
        // Compose HTTP + SSL config in one go. Render's cert chain misses an
        // intermediate in the JVM truststore, so we relax HTTPS validation
        // explicitly. Acceptable in tests; never use in production code.
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", ConfigReader.getInt("http.connect.timeout"))
                        .setParam("http.socket.timeout",     ConfigReader.getInt("http.read.timeout")))
                .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation());
        ExtentManager.get();
    }

    protected String buyerToken()  { return AuthManager.buyerToken();  }
    protected String sellerToken() { return AuthManager.sellerToken(); }
    protected String adminToken()  { return AuthManager.adminToken();  }

    /** Convenience for ad-hoc Extent logging. */
    protected void log(String msg) {
        if (ExtentManager.test() != null) ExtentManager.test().info(msg);
    }
}
