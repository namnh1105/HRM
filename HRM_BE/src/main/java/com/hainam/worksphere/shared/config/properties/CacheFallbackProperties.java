package com.hainam.worksphere.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for cache fallback behavior
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cache.fallback")
public class CacheFallbackProperties {

    /**
     * Enable cache fallback to database when Redis is unavailable
     */
    private boolean enabled = true;

    /**
     * Log level for fallback operations (DEBUG, INFO, WARN, ERROR)
     */
    private String logLevel = "WARN";

    /**
     * Enable periodic Redis connectivity checks
     */
    private boolean periodicHealthCheck = true;

    /**
     * Interval in seconds for Redis health checks
     */
    private int healthCheckIntervalSeconds = 30;

    /**
     * Maximum number of consecutive Redis failures before marking as unavailable
     */
    private int maxConsecutiveFailures = 3;

    /**
     * Enable cache statistics logging
     */
    private boolean enableStats = false;

    /**
     * Interval in seconds for cache statistics reporting
     */
    private int statsReportingIntervalSeconds = 300;
}
