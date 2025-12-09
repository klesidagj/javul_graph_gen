package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import org.example.model.SourceType;

/**
 * Central place for loading application.properties.
 * Provides strongly typed getters for all configuration values.
 */
public class Config {

    private static final Logger log = Logger.getLogger(Config.class.getName());
    private static final String CONFIG_FILE = "application.properties";

    private static Config instance;
    private final Properties props = new Properties();


    // --- Constructor loads the file once ---
    private Config() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new RuntimeException("Missing config: " + CONFIG_FILE);
            }
            props.load(in);
            log.info("Loaded application.properties successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    // --- Singleton ---
    public static synchronized Config get() {
        if (instance == null) instance = new Config();
        return instance;
    }

    public String getDbUrl() {
        return props.getProperty("db.url").trim();
    }

    public String getDbUser() {
        return props.getProperty("db.user").trim();
    }

    public String getDbPassword() {
        return props.getProperty("db.password");
    }

    public String getDbTable() {
        return props.getProperty("db.table", "javul").trim();
    }

    public int getBatchSize() {
        return Integer.parseInt(props.getProperty("db.batchSize", "500"));
    }

    public int getLimit() {
        return Integer.parseInt(props.getProperty("db.limit", "999999999"));
    }

    public boolean isUuidMode() {
        return Boolean.parseBoolean(props.getProperty("db.useUuid", "true"));
    }

    public boolean isAutoDetectParser() {
        return Boolean.parseBoolean(props.getProperty("parser.autoDetect", "true"));
    }

    public SourceType getSourceType() {
        String s = props.getProperty("parser.sourceType", "OTHER");
        return SourceType.fromString(s);
    }

    public boolean isAstEnabled() {
        return Boolean.parseBoolean(props.getProperty("output.astEnabled", "true"));
    }

    public boolean isCfgEnabled() {
        return Boolean.parseBoolean(props.getProperty("output.cfgEnabled", "true"));
    }

    public boolean isDfgEnabled() {
        return Boolean.parseBoolean(props.getProperty("output.dfgEnabled", "true"));
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
}