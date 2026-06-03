package com.cts.mfrp.zuply.tests.ui.auth;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Data-driven login scenarios sourced from AuthData.xlsx.
 *
 * Excel contract (sheet: "Login"):
 *   | TestCaseId | Email           | Password   | ExpectedOutcome |
 *   | TC003      | admin@zuply.in  | Admin@123  | SUCCESS         |
 *   | TC004      | admin@zuply.in  | WrongPass! | INVALID_CREDS   |
 *
 * ExpectedOutcome values understood by this test:
 *   SUCCESS         — login completes, URL leaves /login
 *   INVALID_CREDS   — stays on /login and surfaces "invalid email or password"
 *
 * To plug different test files (CartData, OrderData, ...) in the future,
 * use the same pattern: point {@link #DATA_FILE} + {@link #SHEET} at the new
 * source, and read named columns via the Map-based @DataProvider below.
 */
@Test(groups = {"regression", "ui", "auth"})
public class LoginDataDrivenTest extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/AuthData.xlsx";
    private static final String SHEET     = "Login";

    /* ------------------------------------------------------------------ */
    /* Positional flavour — columns must stay in: id, email, pwd, outcome */
    /* ------------------------------------------------------------------ */

    @DataProvider(name = "loginRows")
    public Object[][] loginRows() throws Exception {
        return ExcelUtils.getTestData(DATA_FILE, SHEET);
    }

    @Test(dataProvider = "loginRows", description = "Data-driven login by row position")
    public void loginByPosition(String testCaseId, String email, String password, String expected) {
        runLoginScenario(testCaseId, email, password, expected);
    }

    /* ------------------------------------------------------------------ */
    /* Named-column flavour — tolerant of column reorders/insertions      */
    /* ------------------------------------------------------------------ */

    @DataProvider(name = "loginRowsAsMaps")
    public Object[][] loginRowsAsMaps() throws Exception {
        return ExcelUtils.getTestDataAsMaps(DATA_FILE, SHEET).stream()
                .map(m -> new Object[]{ m })
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "loginRowsAsMaps", description = "Data-driven login by column name")
    public void loginByName(Map<String, String> row) {
        runLoginScenario(
                row.get("TestCaseId"),
                row.get("Email"),
                row.get("Password"),
                row.get("ExpectedOutcome"));
    }

    /* ------------------------------------------------------------------ */
    /* Shared scenario logic                                              */
    /* ------------------------------------------------------------------ */

    private void runLoginScenario(String testCaseId, String email, String password, String expected) {
        clearSession();                           // ensure no prior login bleeds in
        LoginPage login = new LoginPage(driver);
        login.open();

        if ("SUCCESS".equalsIgnoreCase(expected)) {
            login.loginAs(email, password);       // already waits for URL to leave /login
            Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                    testCaseId + " — expected to leave /login on successful login");
            return;
        }

        if ("INVALID_CREDS".equalsIgnoreCase(expected)) {
            login.enterEmail(email).enterPassword(password).submit();
            wait.until(ExpectedConditions.textMatches(
                    By.tagName("body"),
                    Pattern.compile("invalid email or password", Pattern.CASE_INSENSITIVE)));
            Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                    testCaseId + " — should remain on /login on invalid credentials");
            return;
        }

        Assert.fail(testCaseId + " — unknown ExpectedOutcome: " + expected);
    }
}
