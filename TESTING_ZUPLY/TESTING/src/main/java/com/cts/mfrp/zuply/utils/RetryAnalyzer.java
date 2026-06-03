package com.cts.mfrp.zuply.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int maxRetries = readMax();

    private static int readMax() {
        try { return ConfigReader.getInt("retry.count"); }
        catch (Exception e) { return 1; }
    }

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxRetries) {
            attempt++;
            return true;
        }
        return false;
    }
}
