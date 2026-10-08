package com.hainam.worksphere.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Enable/disable rate limiting globally
     */
    private boolean enabled = true;

    /**
     * Default rate limit - requests per minute for authenticated users
     */
    private int defaultRequestsPerMinute = 100;

    /**
     * Rate limit for login endpoint - requests per minute
     */
    private int loginRequestsPerMinute = 10;

    /**
     * Rate limit for registration endpoint - requests per minute
     */
    private int registerRequestsPerMinute = 5;

    /**
     * Rate limit for refresh token endpoint - requests per minute
     */
    private int refreshTokenRequestsPerMinute = 30;

    /**
     * Rate limit for anonymous/unauthenticated users - requests per minute
     */
    private int anonymousRequestsPerMinute = 50;

    /**
     * Ban duration in minutes when rate limit is exceeded multiple times
     */
    private int banDurationMinutes = 15;

    /**
     * Number of rate limit violations before temporary ban
     */
    private int maxViolationsBeforeBan = 5;
}

