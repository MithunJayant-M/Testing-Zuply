package com.cts.mfrp.zuply.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExcelUtils — reads test data from .xlsx files via Apache POI for TestNG
 * {@code @DataProvider}s.
 *
 * Sheet contract:
 *   Row 0  = headers (used as keys for {@link #getTestDataAsMaps})
 *   Row 1+ = data rows
 *
 * Numeric cells are stringified via {@link DataFormatter}, which preserves the
 * cell's display format — so "9876543210" stays as digits instead of becoming
 * "9.87654321E9". Blank rows are skipped.
 *
 * Usage:
 *   <pre>
 *   {@literal @}DataProvider(name = "loginData")
 *   public Object[][] loginData() throws Exception {
 *       return ExcelUtils.getTestData(
 *           "src/test/resources/testdata/AuthData.xlsx", "Login");
 *   }
 *   </pre>
 */
public final class ExcelUtils {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private ExcelUtils() {}

    /**
     * Read all data rows (skipping header row 0 and any blank rows) as a 2D
     * Object matrix — the shape TestNG's {@code @DataProvider} expects.
     */
    public static Object[][] getTestData(String filePath, String sheetName) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = requireSheet(wb, sheetName);
            int cols = sheet.getRow(0).getLastCellNum();
            List<Object[]> rows = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isBlankRow(row)) continue;

                Object[] data = new Object[cols];
                for (int c = 0; c < cols; c++) {
                    data[c] = cellAsString(row.getCell(c));
                }
                rows.add(data);
            }
            return rows.toArray(new Object[0][]);
        }
    }

    /**
     * Same data, but each row is a {@code Map<header, value>}. Prefer this
     * when tests reference fields by name — column reorders won't break tests.
     *
     * In a {@code @DataProvider}:
     *   <pre>
     *   return ExcelUtils.getTestDataAsMaps(path, sheet).stream()
     *           .map(m -&gt; new Object[]{ m })
     *           .toArray(Object[][]::new);
     *   </pre>
     */
    public static List<Map<String, String>> getTestDataAsMaps(String filePath, String sheetName)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = requireSheet(wb, sheetName);
            Row header = sheet.getRow(0);
            int cols = header.getLastCellNum();

            List<String> headers = new ArrayList<>(cols);
            for (int c = 0; c < cols; c++) {
                headers.add(cellAsString(header.getCell(c)));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isBlankRow(row)) continue;

                Map<String, String> map = new LinkedHashMap<>();
                for (int c = 0; c < cols; c++) {
                    map.put(headers.get(c), cellAsString(row.getCell(c)));
                }
                rows.add(map);
            }
            return rows;
        }
    }

    /** Read a single cell by 0-based row/column index. */
    public static String getCellData(String filePath, String sheetName, int row, int col)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {
            return cellAsString(requireSheet(wb, sheetName).getRow(row).getCell(col));
        }
    }

    /**
     * Look up a single row by its {@code TestCaseId} column value. Returns the
     * first match (case-insensitive). Throws {@link IllegalArgumentException}
     * when no row matches — callers should treat that as a missing-fixture bug,
     * not a runtime branch.
     *
     * Convenience for tests that need a single fixture row per method instead
     * of iterating an entire sheet via {@code @DataProvider}.
     */
    public static Map<String, String> getRowByTestCaseId(
            String filePath, String sheetName, String testCaseId) throws IOException {
        for (Map<String, String> row : getTestDataAsMaps(filePath, sheetName)) {
            if (testCaseId.equalsIgnoreCase(row.getOrDefault("TestCaseId", ""))) {
                return row;
            }
        }
        throw new IllegalArgumentException(
                "No row with TestCaseId=" + testCaseId + " in " + filePath + " sheet " + sheetName);
    }

    private static Sheet requireSheet(Workbook wb, String name) {
        Sheet s = wb.getSheet(name);
        if (s == null) {
            throw new IllegalArgumentException("Sheet not found: " + name);
        }
        return s;
    }

    private static String cellAsString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.FORMULA) {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook()
                    .getCreationHelper().createFormulaEvaluator();
            return FORMATTER.formatCellValue(cell, evaluator).trim();
        }
        return FORMATTER.formatCellValue(cell).trim();
    }

    private static boolean isBlankRow(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (!cellAsString(row.getCell(c)).isEmpty()) return false;
        }
        return true;
    }
}
