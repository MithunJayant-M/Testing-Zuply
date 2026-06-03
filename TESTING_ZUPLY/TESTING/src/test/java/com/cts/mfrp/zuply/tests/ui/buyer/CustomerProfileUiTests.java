package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Customer profile — FRD §2.1 (profile view). Maps to TC019.
 * (AD_TC020 — ProfilePictureUpload and TC033 — ProfileUpdateOptionsPresent
 *  removed per cleanup pass.)
 */
@Test(groups = {"regression", "ui", "profile"})
public class CustomerProfileUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Profile");
        loginViaUi(buyerEmail, defaultPassword());
    }

    /** TC019 — Customer can view profile information. */
    @Test(description = "TC019 — CustomerProfileEdit")
    public void tc019_customerProfileEdit() {
        ProfilePage page = new ProfilePage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Profile card should be visible");
        String displayedEmail = page.displayedEmail();
        Assert.assertEquals(displayedEmail.toLowerCase().trim(), buyerEmail.toLowerCase(),
                "Profile should show the registered email");
    }
}
