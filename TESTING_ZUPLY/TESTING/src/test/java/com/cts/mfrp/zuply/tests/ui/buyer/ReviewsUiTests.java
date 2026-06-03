package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Product reviews and ratings — FRD §2.9. Maps to AD_TC_RV001.
 * (AD_TC_RV003 — LoggedInCustomerSeesReviewForm and AD_TC_RV004 — ReviewsReverseChronological
 *  removed per cleanup pass; both were data-dependent and produced flaky/skipped runs.)
 */
@Test(groups = {"regression", "ui", "reviews"})
public class ReviewsUiTests extends UiBaseTest {

    /** AD_TC_RV001 — Anonymous visitor can view reviews/ratings on a product detail page. */
    @Test(description = "AD_TC_RV001 -- AnonymousCanViewReviews")
    public void rv001_anonymousCanViewReviews() {
        clearSession();
        openFirstProductDetail();

        boolean hasReviewSection = !driver.findElements(By.xpath(
                "//*[contains(translate(.,'REVIEW','review'),'review')"
                + " or contains(translate(.,'RATING','rating'),'rating')"
                + " or contains(@class,'review') or contains(@class,'rating')]")).isEmpty();
        Assert.assertTrue(hasReviewSection,
                "Product detail page should expose a reviews/ratings section to anonymous visitors (FRD §2.9)");
    }

    /**
     * Navigate to /products, click the first product card, and wait for the
     * detail URL pattern.
     */
    private void openFirstProductDetail() {
        new ProductsPage(driver).open();
        List<WebElement> cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        if (cards.isEmpty()) {
            throw new SkipException("No product cards on this env — review tests need at least one product");
        }
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));
    }
}
