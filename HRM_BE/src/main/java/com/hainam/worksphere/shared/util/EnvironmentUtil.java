package com.hainam.worksphere.shared.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Utility class for accessing environment variables with type safety and defaults
 */
@Component
public class EnvironmentUtil {

    private final Environment environment;

    @Autowired
    public EnvironmentUtil(Environment environment) {
        this.environment = environment;
    }

    public String getString(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public String getRequiredString(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property '" + key + "' is not set");
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        return environment.getProperty(key, Integer.class, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return environment.getProperty(key, Long.class, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return environment.getProperty(key, Boolean.class, defaultValue);
    }

    public String getActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : "default";
    }

    public boolean isDevelopment() {
        return "development".equals(getActiveProfile());
    }

    public boolean isProduction() {
        return "production".equals(getActiveProfile());
    }
}
