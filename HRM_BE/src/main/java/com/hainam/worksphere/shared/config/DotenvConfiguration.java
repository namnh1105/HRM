package com.hainam.worksphere.shared.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Configuration class for dotenv handling
 */
@Component
public class DotenvConfiguration {
    public static void validateRequiredEnvironmentVariables(Environment environment) {
        String[] requiredVars = {
            "JWT_SECRET",
            "DB_HOST",
            "DB_PORT",
            "DB_NAME",
            "DB_USERNAME",
            "DB_PASSWORD",
            "SERVER_PORT",
            "FRONTEND_URL",
            "OAUTH_SUCCESS_REDIRECT_PATH"
        };

        for (String var : requiredVars) {
            String value = environment.getProperty(var);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException(
                    String.format("Required environment variable '%s' is not set. " +
                        "Please check your .env file or system environment variables.", var)
                );
            }
        }
    }
}
