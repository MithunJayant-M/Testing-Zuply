package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Read-only profile/account view at {@code /profile}. The SPA displays the
 * user's avatar, name, email, phone — there's no inline edit form on this
 * page; the avatar file input lets the user upload an image, and the logout
 * button signs them out.
 */
public class ProfilePage extends BasePage {

    private static final By PROFILE_CARD = By.cssSelector(".profile-card");
    private static final By PROFILE_NAME = By.cssSelector(".profile-name");
    private static final By PROFILE_EMAIL = By.cssSelector(".profile-email");
    private static final By PROFILE_PHONE = By.cssSelector(".profile-phone");
    private static final By PROFILE_AVATAR = By.cssSelector(".profile-avatar");
    private static final By AVATAR_FILE_INPUT = By.cssSelector("input[type='file'][accept='image/*']");
    private static final By LOGOUT_BTN = By.xpath("//button[contains(@class,'btn-danger') and normalize-space()='Logout']");
    private static final By QUICK_LINKS = By.cssSelector(".profile-quick-links a");

    public ProfilePage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/profile"; }
    @Override protected By readyMarker() { return PROFILE_CARD; }

    public String displayedName()  { return text(PROFILE_NAME); }
    public String displayedEmail() { return text(PROFILE_EMAIL); }
    public String displayedPhone() { return text(PROFILE_PHONE); }

    public void uploadAvatar(String absolutePath) {
        // The file input is display:none; sendKeys still works on it
        driver.findElement(AVATAR_FILE_INPUT).sendKeys(absolutePath);
    }

    public void logout() { click(LOGOUT_BTN); }

    public int quickLinkCount() { return driver.findElements(QUICK_LINKS).size(); }
}
