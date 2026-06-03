package com.cts.mfrp.zuply.utils;

import com.cts.mfrp.zuply.constants.AppConstants;

import java.io.IOException;
import java.util.UUID;

/**
 * Wrapper around ExcelUtils that:
 *   1. Resolves ${rand} tokens with a random alphanumeric suffix (per-row).
 *   2. Builds the absolute path under {@link AppConstants#TESTDATA_DIR}.
 */
public final class TestDataHelper {

    private TestDataHelper() {}

    public static Object[][] read(String fileName, String sheet) throws IOException {
        Object[][] data = ExcelUtils.getTestData(AppConstants.TESTDATA_DIR + fileName, sheet);
        for (Object[] row : data) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            for (int c = 0; c < row.length; c++) {
                if (row[c] instanceof String s && s.contains("${rand}")) {
                    row[c] = s.replace("${rand}", suffix);
                }
            }
        }
        return data;
    }
}
