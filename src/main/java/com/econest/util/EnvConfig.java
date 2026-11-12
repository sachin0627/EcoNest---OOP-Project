package com.econest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to load environment variables from an external `.env` file
 * or fallback to system environment variables and default properties.
 * 
 * Ensures security: No credentials hardcoded in Java source files.
 */
public class EnvConfig {
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static boolean loaded = false;

    private EnvConfig() {}

    public static synchronized void init() {
        if (loaded) return;
        
        // Search locations for .env: current directory, parent directory, user dir
        String[] possiblePaths = {
            ".env",
            "econest/.env",
            System.getProperty("user.dir") + File.separator + ".env"
        };

        for (String path : possiblePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                loadFromFile(envFile);
                break;
            }
        }
        loaded = true;
    }

    private static void loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    // Strip enclosing quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    ENV_VARS.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("[EnvConfig] Warning: Could not read .env file: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        if (!loaded) init();
        
        // 1. Check loaded .env map
        String val = ENV_VARS.get(key);
        if (val != null && !val.isEmpty()) return val;

        // 2. Check System environment
        val = System.getenv(key);
        if (val != null && !val.isEmpty()) return val;

        // 3. Check System properties
        val = System.getProperty(key);
        if (val != null && !val.isEmpty()) return val;

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }
}
