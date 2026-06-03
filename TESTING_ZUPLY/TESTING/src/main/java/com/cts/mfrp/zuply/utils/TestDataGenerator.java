package com.cts.mfrp.zuply.utils;

import com.cts.mfrp.zuply.constants.AppConstants;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * One-time generator for the project's Excel test data files.
 * Run as: mvn -q exec:java -Dexec.mainClass="com.cts.mfrp.zuply.utils.TestDataGenerator"
 *      OR right-click -> Run main() in IDE.
 *
 * Safe to re-run; overwrites existing files in src/test/resources/testdata/.
 */
public class TestDataGenerator {

    public static void main(String[] args) throws IOException {
        new File(AppConstants.TESTDATA_DIR).mkdirs();
        writeAuthData();
        writeUserData();
        writeProductData();
        writeReviewData();
        writeCartData();
        writeOrderData();
        writeSellerData();
        writeListingData();
        System.out.println("Test data files generated in " + AppConstants.TESTDATA_DIR);
    }

    /* ------------------------------------------------------------------ */
    /* AuthData.xlsx                                                       */
    /* ------------------------------------------------------------------ */
    private static void writeAuthData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            Sheet reg = wb.createSheet("Register");
            writeRow(reg, 0, "name", "email", "password", "role", "phone", "expectedStatus", "description");
            writeRow(reg, 1, "John Buyer", "john.buyer.${rand}@gmail.com", "Test@1234", "CUSTOMER", "9876543210", "200", "valid registration");

            Sheet login = wb.createSheet("Login");
            writeRow(login, 0, "email", "password", "expectedStatus", "description");
            writeRow(login, 1, "admin@zuply.in", "Admin@123", "200", "valid admin login");

            save(wb, "AuthData.xlsx");
        }
    }

    private static void writeUserData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("ProfileUpdate");
            writeRow(s, 0, "name", "email", "expectedStatus", "description");
            writeRow(s, 1, "Updated Buyer Name", "updated.buyer.${rand}@gmail.com", "200", "valid update");
            save(wb, "UserData.xlsx");
        }
    }

    private static void writeProductData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet create = wb.createSheet("Create");
            writeRow(create, 0, "name", "description", "categoryId", "price", "stock", "expectedStatus", "description_tc");
            writeRow(create, 1, "Wireless Bluetooth Headphones", "Premium noise-cancelling headphones", "5", "4999", "100", "200", "valid product");

            Sheet search = wb.createSheet("Search");
            writeRow(search, 0, "keyword", "category", "minPrice", "maxPrice", "sort", "expectedStatus", "description");
            writeRow(search, 1, "phone", "", "", "", "", "200", "keyword only");

            save(wb, "ProductData.xlsx");
        }
    }

    private static void writeReviewData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Submit");
            writeRow(s, 0, "rating", "comment", "expectedStatus", "description");
            writeRow(s, 1, "5", "Amazing product, would highly recommend!", "201", "valid review");
            save(wb, "ReviewData.xlsx");
        }
    }

    private static void writeCartData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("AddItem");
            writeRow(s, 0, "productId", "quantity", "expectedStatus", "description");
            writeRow(s, 1, "1", "1", "201", "valid add");
            save(wb, "CartData.xlsx");
        }
    }

    private static void writeOrderData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Place");
            writeRow(s, 0, "address", "city", "pincode", "paymentMethod", "expectedStatus", "description");
            writeRow(s, 1, "42, Anna Nagar, Chennai", "Chennai", "600040", "COD", "200", "valid place order");
            save(wb, "OrderData.xlsx");
        }
    }

    private static void writeSellerData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet reg = wb.createSheet("Register");
            writeRow(reg, 0, "shopName", "description", "contactNumber", "address", "pincode", "expectedStatus", "description_tc");
            writeRow(reg, 1, "TechGadgets Store", "One-stop shop", "9876543210", "15, T Nagar, Chennai", "600017", "201", "valid seller registration");

            Sheet status = wb.createSheet("OrderStatus");
            writeRow(status, 0, "status", "expectedStatus", "description");
            writeRow(status, 1, "PROCESSING", "200", "valid PROCESSING");

            save(wb, "SellerData.xlsx");
        }
    }

    private static void writeListingData() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Edit");
            writeRow(s, 0, "title", "description", "category", "price", "stock", "expectedStatus", "description_tc");
            writeRow(s, 1, "Sony WH-1000XM5 Style Headphones", "Updated professional-grade audio", "Electronics", "5499", "75", "200", "valid edit");
            save(wb, "ListingData.xlsx");
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                             */
    /* ------------------------------------------------------------------ */
    private static void writeRow(Sheet sheet, int rowIdx, String... cells) {
        Row row = sheet.createRow(rowIdx);
        for (int c = 0; c < cells.length; c++) {
            row.createCell(c).setCellValue(cells[c]);
        }
    }

    private static void save(XSSFWorkbook wb, String fileName) throws IOException {
        try (FileOutputStream out = new FileOutputStream(AppConstants.TESTDATA_DIR + fileName)) {
            wb.write(out);
        }
    }
}
