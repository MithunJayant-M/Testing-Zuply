package com.cts.mfrp.zuply.utils;


import java.io.*;
import java.util.Properties;

public class ConfigReader {
    private static Properties props;

    static {
        try {
            String env = System.getProperty("env", "prod");
            String file = "src/test/resources/config-" + env + ".properties";
            File f = new File(file);
            if (!f.exists()) file = "src/test/resources/config.properties";
            props = new Properties();
            props.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp;
        String val = props.getProperty(key);
        if (val == null) throw new RuntimeException("Key not found: " + key);
        return val.trim();
    }

    public static int     getInt(String k)     { return Integer.parseInt(get(k)); }
    public static boolean getBoolean(String k) { return Boolean.parseBoolean(get(k)); }
}