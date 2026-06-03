package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.UploadClient;
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

@Test(groups = {"regression", "api", "upload"})
public class UploadTests extends BaseTest {

    private UploadClient client;
    private File sampleImage;

    @BeforeClass
    public void setUp() throws IOException {
        client = new UploadClient();

        Path p = Files.createTempFile("zuply_sample_", ".jpg");
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(33, 150, 243));
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.WHITE);
        g.drawString("ZUPLY", 80, 100);
        g.dispose();
        ImageIO.write(img, "jpg", p.toFile());
        sampleImage = p.toFile();
        sampleImage.deleteOnExit();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "POST /api/upload with valid JPEG and seller JWT -> 200 with imageId")
    public void testUploadValidImage() {
        client.uploadFile(sellerToken(), sampleImage)
            .then()
                .statusCode(200)
                .body("data.imageId", notNullValue());
    }

    @Test(description = "POST /api/upload as buyer -> 403 Forbidden")
    public void testUploadAsBuyerForbidden() {
        client.uploadFile(buyerToken(), sampleImage)
            .then()
                .statusCode(403);
    }
}
