package com.cts.mfrp.zuply.tests.ui.ai;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.SellerUploadPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * AI-powered listing pipeline — FRD §2.10 (Product Upload) + §4.2 (AI flow) + §3.1 (perf).
 * Covers TC029 – TC038. These tests drive the SPA seller upload flow end-to-end:
 *
 *   image upload → background processing → Gemini AI generation → preview/edit →
 *   publish → admin approval → public visibility.
 *
 * Several of these depend on Gemini availability + AI Vision API quota; tests will
 * skip cleanly when the AI pipeline is unreachable rather than failing the suite.
 *
 * All waits are explicit via SellerUploadPage.waitForAiContent or
 * BasePage.waitForUrlContains.
 */
@Test(groups = {"regression", "ui", "ai"})
public class AIListingUiTests extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_ProductData.xlsx";
    private static final String SHEET     = "AIListing";

    private static final Duration UPLOAD_SETTLE = Duration.ofSeconds(10);
    private static final Duration AI_CONTENT    = Duration.ofSeconds(30);
    private static final Duration E2E_LIMIT     = Duration.ofSeconds(90);

    private Map<String, String> data(String testCaseId) throws IOException {
        return ExcelUtils.getRowByTestCaseId(DATA_FILE, SHEET, testCaseId);
    }

    private String sellerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginSeller() {
        sellerEmail = registerNewSeller("AISeller");
        loginViaUi(sellerEmail, defaultPassword());
    }

    @BeforeMethod(alwaysRun = true)
    public void resetFormState() {
        navigateRoute("/seller/dashboard");
    }

    /**
     * Helper to mimic the exact user flow shown in the UI. Stock / delivery /
     * return values come from the AIListing sheet so each test scenario can
     * tweak its inputs without code edits.
     */
    private void setupAndGenerate(SellerUploadPage page, Map<String, String> row) throws IOException {
        page.selectAiMode();
        page.uploadImage(generateJpeg(400, 400));
        page.enterStock(row.get("Stock"));
        page.selectOptionByText(row.get("DeliveryOption"));
        page.selectOptionByText(row.get("ReturnOption"));
        page.clickGenerate();
    }

    /** SMART POLLER: Instantly fails if the backend crashes, otherwise waits for AI content */
    private void waitForAiOrFail(SellerUploadPage page, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(d -> {
                if (page.exists(By.xpath("//*[contains(text(), 'Pipeline failed')]"))) {
                    Assert.fail("Backend crashed! The red 'Pipeline failed' banner appeared.");
                }
                return page.hasAiContentGenerated();
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            Assert.fail("AI content failed to generate within " + timeout.toSeconds() + "s. No error banner appeared.");
        }
    }

    @Test(description = "TC029 — BackgroundRemoval")
    public void tc029_backgroundRemoval() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        page.uploadImage(generateJpeg(400, 400));
        page.waitForUploadAccepted(UPLOAD_SETTLE);
        Assert.assertTrue(page.isLoaded(), "Upload page should remain loaded");
    }

    @Test(description = "TC030 — ProcessingTime (perf)")
    public void tc030_processingTime() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        long start = System.currentTimeMillis();
        page.uploadImage(generateJpeg(300, 300));
        page.waitForUploadAccepted(Duration.ofSeconds(30));
        long elapsedMs = System.currentTimeMillis() - start;
        Assert.assertTrue(elapsedMs < 30_000, "Processing took too long: " + elapsedMs + "ms");
    }

    @Test(description = "TC031 — AIContentGeneration")
    public void tc031_aiContentGeneration() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC031"));

        waitForAiOrFail(page, AI_CONTENT);
        Assert.assertFalse(page.generatedTitle().isBlank(), "Title should be non-empty");
    }

    @Test(description = "TC032 — TagGeneration")
    public void tc032_tagGeneration() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC032"));

        waitForAiOrFail(page, AI_CONTENT);
        int tags = page.tagCount();
        Assert.assertTrue(tags >= 5 && tags <= 10, "Expected 5-10 tags, got " + tags);
    }

    @Test(description = "TC033 — CategoryAutoAssign")
    public void tc033_categoryAutoAssign() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC033"));

        waitForAiOrFail(page, AI_CONTENT);
        Assert.assertFalse(page.selects().isEmpty(), "No category select rendered");
        Assert.assertNotNull(page.firstSelectValue(), "Category select should have a value");
    }

    @Test(description = "TC034 — HighlightGeneration")
    public void tc034_highlightGeneration() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC034"));

        waitForAiOrFail(page, AI_CONTENT);
        int highlights = page.highlightCount();
        Assert.assertTrue(highlights >= 3 && highlights <= 5, "Expected 3-5 highlights, got " + highlights);
    }

    @Test(description = "TC035 — ListingPreviewDisplay")
    public void tc035_listingPreviewDisplay() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC035"));

        waitForAiOrFail(page, AI_CONTENT);
        Assert.assertTrue(page.isLoaded(), "Preview should be visible");
    }

    @Test(description = "TC036 — ListingEditing")
    public void tc036_listingEditing() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        Map<String, String> row = data("TC036");
        setupAndGenerate(page, row);

        waitForAiOrFail(page, AI_CONTENT);
        page.enterTitle(row.get("Title"));
        page.enterPrice(row.get("Price"));
        Assert.assertTrue(page.isLoaded(), "Page should remain usable");
    }

    @Test(description = "TC037 — PublishListing")
    public void tc037_publishListing() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();
        setupAndGenerate(page, data("TC037"));

        waitForAiOrFail(page, AI_CONTENT);

        // After AI finishes, click the final submit button to publish the listing
        page.submitForReview();
        waitAfterAction();
        Assert.assertFalse(page.title().contains("Page not found"), "Publish should not land on 404");
    }

    @Test(description = "TC038 — EndToEndListingTime (perf)")
    public void tc038_endToEndListingTime() throws IOException {
        SellerUploadPage page = new SellerUploadPage(driver);
        page.open();

        long start = System.currentTimeMillis();
        setupAndGenerate(page, data("TC038"));
        waitForAiOrFail(page, E2E_LIMIT);
        page.submitForReview();

        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue(elapsed < 90_000, "Full pipeline took too long: " + elapsed + "ms");
    }

//    // ADDITINAL TEST CASE
//    /** TC037 — Seller can publish a draft listing for admin review. */
//    @Test(description = "AD_TC037.5 — PublishListing")
//    public void adtc037_publishListing() throws IOException {
//        SellerUploadPage page = new SellerUploadPage(driver);
//        page.open();
//        page.uploadImage(generateJpeg(400, 400));
//        page.waitForAiContent(AI_CONTENT);
//        try { page.submitForReview(); }
//        catch (Exception e) {
//            throw new SkipException("Submit button not interactable: " + e.getMessage());
//        }
//        waitAfterAction();
//        Assert.assertFalse(page.title().contains("Page not found"),
//                "Publish should not land on Netlify 404");
//    }
//    /** AD_TC038 — Full listing pipeline completes within 90 seconds (FRD §3.1). */
//
//    @Test(description = "AD_TC038 — EndToEndListingTime (perf)")
//
//    public void adtc038_endToEndListingTime() throws IOException {
//
//        SellerUploadPage page = new SellerUploadPage(driver);
//        page.open();
//        long start = System.currentTimeMillis();
//        page.uploadImage(generateJpeg(400, 400));
//        boolean ready = page.waitForAiContent(E2E_LIMIT);
//        long elapsed = System.currentTimeMillis() - start;
//        if (!ready) {
//            throw new SkipException("AI pipeline did not produce content within "
//                    + E2E_LIMIT.toSeconds() + "s — can't measure end-to-end time");
//        }
//        Assert.assertTrue(elapsed < 90_000,
//                "Full pipeline should complete in under 90s per FRD §3.1; was " + elapsed + "ms");
//    }

    private static File generateJpeg(int w, int h) throws IOException {
        Path p = Files.createTempFile("zuply_ai_", ".jpg");
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(76, 175, 80));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.drawString("ZUPLY", w / 2 - 20, h / 2);
        g.dispose();
        ImageIO.write(bi, "jpg", p.toFile());
        File f = p.toFile();
        f.deleteOnExit();
        return f;
    }
}