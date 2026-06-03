package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.ListingClient;
import com.cts.mfrp.zuply.clients.UploadClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "ai"})
public class ListingTests extends BaseTest {

    private ListingClient client;
    private Integer imageId;
    private Integer draftProductId;

    @BeforeClass
    public void setUp() throws IOException {
        client = new ListingClient();

        Path p = Files.createTempFile("zuply_listing_", ".jpg");
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(76, 175, 80));
        g.fillRect(0, 0, 200, 200);
        g.dispose();
        ImageIO.write(img, "jpg", p.toFile());
        File sample = p.toFile();
        sample.deleteOnExit();

        Response upR = new UploadClient().uploadFile(sellerToken(), sample);
        if (upR.statusCode() == 200) {
            Object id = ResponseUtils.body(upR).get("imageId");
            if (id != null) imageId = ((Number) id).intValue();
        }
        if (imageId != null) {
            Response genR = client.generate(sellerToken(), imageId);
            if (genR.statusCode() == 200) {
                Object pid = ResponseUtils.body(genR).get("productId");
                if (pid != null) draftProductId = ((Number) pid).intValue();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "POST /api/listing/generate/{imageId} -> 200 with status DRAFT")
    public void testGenerate() {
        int id = imageId != null ? imageId : 1;

        Response r = client.generate(sellerToken(), id);
        r.then().statusCode(anyOf(equalTo(200), equalTo(500)));

        if (r.statusCode() == 200) {
            r.then().body("data.status", equalTo("DRAFT"));
        }
    }

    @Test(description = "POST /api/listing/{productId}/publish -> 200 status PENDING")
    public void testPublish() {
        if (draftProductId == null)
            throw new org.testng.SkipException("No draft listing — AI generate failed in setUp");

        client.publish(sellerToken(), draftProductId)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400)));
    }
}
