package com.cts.mfrp.zuply.utils;

import com.cts.mfrp.zuply.constants.AppConstants;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * One-shot, idempotent augmenter for the existing UI_*Data.xlsx workbooks.
 *
 * Why a separate class from {@code TestDataGenerator}?
 *   {@code TestDataGenerator} creates the AuthData/ProductData/... workbooks
 *   from scratch and would clobber any hand-authored UI rows. This augmenter
 *   opens each {@code UI_*Data.xlsx} file in place and appends only the sheets
 *   referenced by the newly-data-driven UI tests — preserving everything else.
 *
 * Run once after pulling these changes:
 *   mvn -q exec:java -Dexec.mainClass="com.cts.mfrp.zuply.utils.UiTestDataAugmenter"
 *
 * Safe to re-run: any sheet that already exists is left untouched.
 */
public final class UiTestDataAugmenter {

    private UiTestDataAugmenter() {}

    private static volatile boolean DID_RUN = false;

    /**
     * Idempotent entry point used both by {@link #main(String[])} (manual mvn
     * exec) and by {@code UiBaseTest}'s static initializer (auto-run on first
     * test JVM startup). Safe to call from multiple threads; the second caller
     * returns immediately.
     */
    public static synchronized void run() throws IOException {
        if (DID_RUN) return;
        augmentAuthData();
        augmentProductData();
        DID_RUN = true;
    }

    public static void main(String[] args) throws IOException {
        run();
        System.out.println("Done. UI_*Data.xlsx sheets are in sync.");
    }

    /* ------------------------------------------------------------------ */
    /* UI_AuthData.xlsx                                                    */
    /* ------------------------------------------------------------------ */
    private static void augmentAuthData() throws IOException {
        String file = AppConstants.TESTDATA_DIR + "UI_AuthData.xlsx";
        try (XSSFWorkbook wb = openOrCreate(file)) {

            if (wb.getSheet("Defaults") == null) {
                Sheet s = wb.createSheet("Defaults");
                writeRow(s, 0, "Key", "Value", "Description");
                writeRow(s, 1, "DefaultPassword", "Test@1234",
                        "Shared password for auto-registered customers/sellers");
                writeRow(s, 2, "DefaultPhone", "9876543210",
                        "Shared phone for auto-registered customers/sellers");
            }

            if (wb.getSheet("AdminCreate") == null) {
                Sheet s = wb.createSheet("AdminCreate");
                writeRow(s, 0, "TestCaseId", "Scenario", "Name", "Email",
                        "Password", "Phone", "Description");
                writeRow(s, 1, "TC069", "KNOWN_BUG", "TestAdmin",
                        "testadmin@zuply.in", "Admin@123", "9876543210",
                        "Backend api/admin/create-admin not implemented (known bug)");
            }
            save(wb, file);
        }
    }

    /* ------------------------------------------------------------------ */
    /* UI_ProductData.xlsx                                                 */
    /* ------------------------------------------------------------------ */
    private static void augmentProductData() throws IOException {
        String file = AppConstants.TESTDATA_DIR + "UI_ProductData.xlsx";
        try (XSSFWorkbook wb = openOrCreate(file)) {

            if (wb.getSheet("AIListing") == null) {
                Sheet s = wb.createSheet("AIListing");
                writeRow(s, 0, "TestCaseId", "Scenario", "Stock", "DeliveryOption",
                        "ReturnOption", "Title", "Price", "Description");
                writeRow(s, 1, "TC031", "GENERATE", "10", "Home Delivery",
                        "No Returns", "", "", "AI content generation");
                writeRow(s, 2, "TC032", "GENERATE", "10", "Home Delivery",
                        "No Returns", "", "", "Tag generation 5-10 tags");
                writeRow(s, 3, "TC033", "GENERATE", "10", "Home Delivery",
                        "No Returns", "", "", "Category auto-assign");
                writeRow(s, 4, "TC034", "GENERATE", "10", "Home Delivery",
                        "No Returns", "", "", "Highlights 3-5");
                writeRow(s, 5, "TC035", "GENERATE", "10", "Home Delivery",
                        "No Returns", "", "", "Preview display");
                writeRow(s, 6, "TC036", "EDIT", "10", "Home Delivery",
                        "No Returns", "Premium Basmati Rice 1kg", "299",
                        "Edit listing after AI generation");
                writeRow(s, 7, "TC037", "PUBLISH", "10", "Home Delivery",
                        "No Returns", "", "", "Publish listing");
                writeRow(s, 8, "TC038", "E2E", "10", "Home Delivery",
                        "No Returns", "", "", "End-to-end pipeline timing");
            }

            if (wb.getSheet("SellerUpload") == null) {
                Sheet s = wb.createSheet("SellerUpload");
                writeRow(s, 0, "TestCaseId", "Scenario", "TitlePrefix",
                        "ProductDescription", "Price", "Stock", "Description");
                writeRow(s, 1, "TC020", "CREATE", "UI Test Rice",
                        "1 kg pack, organic", "80", "100", "Seller creates a product listing");
                writeRow(s, 2, "TC028", "FORM_FIELDS", "Smoke Title",
                        "Smoke description text for automated test", "99", "10",
                        "Seller upload form fields present");
            }

            if (wb.getSheet("ImageUpload") == null) {
                Sheet s = wb.createSheet("ImageUpload");
                writeRow(s, 0, "TestCaseId", "Scenario", "Format", "Width",
                        "Height", "SizeMB", "Description");
                writeRow(s, 1, "TC025", "VALID_JPEG", "jpg", "200", "200", "0",
                        "Valid JPEG within size limit");
                writeRow(s, 2, "TC026", "INVALID_TYPE", "pdf", "0", "0", "0",
                        "Reject non-image type (PDF)");
                writeRow(s, 3, "TC027", "OVERSIZED", "jpg", "0", "0", "12",
                        "Reject file size exceeding 10MB");
                writeRow(s, 4, "TC028", "VALID_PNG", "png", "200", "200", "0",
                        "Valid PNG within size limit");
            }
            save(wb, file);
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                             */
    /* ------------------------------------------------------------------ */

    private static XSSFWorkbook openOrCreate(String filePath) throws IOException {
        Path p = Paths.get(filePath);
        if (Files.exists(p)) {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                return new XSSFWorkbook(fis);
            }
        }
        return new XSSFWorkbook();
    }

    private static void writeRow(Sheet sheet, int rowIdx, String... cells) {
        Row row = sheet.createRow(rowIdx);
        for (int c = 0; c < cells.length; c++) {
            row.createCell(c).setCellValue(cells[c]);
        }
    }

    private static void save(XSSFWorkbook wb, String filePath) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            wb.write(out);
        }
    }
}
