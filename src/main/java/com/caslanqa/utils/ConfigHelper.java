package com.caslanqa.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigHelper {
    private static Map<String, Object> config;

    static {
        try (InputStream in = ConfigHelper.class.getClassLoader().getResourceAsStream("config.yml")) {
            if (in == null) {
                throw new RuntimeException("config.yml bulunamadı!");
            }
            Yaml yaml = new Yaml();
            config = yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Config dosyası okunamadı: " + e.getMessage(), e);
        }
    }

    public static String getDatabasePath() {
        String rawPath = ((Map<String, String>) config.get("database")).get("path");
        // ${user.home} değişkenini replace et
        return rawPath.replace("${user.home}", System.getProperty("user.home"));
    }
}
