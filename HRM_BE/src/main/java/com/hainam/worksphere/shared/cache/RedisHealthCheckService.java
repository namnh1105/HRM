package com.hainam.worksphere.shared.cache;

import com.hainam.worksphere.shared.config.properties.CacheFallbackProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for monitoring Redis health and providing fallback status information
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.cache.fallback.enabled", havingValue = "true", matchIfMissing = true)
public class RedisHealthCheckService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final CacheFallbackProperties properties;

    private final AtomicBoolean redisAvailable = new AtomicBoolean(true);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile Instant lastSuccessfulCheck;
    private volatile Instant lastFailedCheck;
    private volatile String lastErrorMessage;

    /**
     * Periodic health check for Redis connectivity
     */
    @Scheduled(fixedDelayString = "#{${app.cache.fallback.health-check-interval-seconds:30} * 1000}")
    public void performHealthCheck() {
        if (!properties.isPeriodicHealthCheck()) {
            return;
        }

        try {
            // Perform a simple Redis operation to test connectivity
            String testKey = "health:check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", java.time.Duration.ofSeconds(10));
            redisTemplate.delete(testKey);

            // Mark as successful
            markHealthCheckSuccess();

        } catch (Exception e) {
            markHealthCheckFailure(e);
        }
    }

    /**
     * Check if Redis is currently available
     */
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }

    /**
     * Get the number of consecutive failures
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    /**
     * Get the last successful check time
     */
    public Instant getLastSuccessfulCheck() {
        return lastSuccessfulCheck;
    }

    /**
     * Get the last failed check time
     */
    public Instant getLastFailedCheck() {
        return lastFailedCheck;
    }

    /**
     * Get the last error message
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Manually test Redis connectivity
     */
    public boolean testRedisConnectivity() {
        try {
            String testKey = "manual:health:check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", java.time.Duration.ofSeconds(5));
            String result = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean success = "test".equals(result);
            if (success) {
                markHealthCheckSuccess();
            } else {
                markHealthCheckFailure(new RuntimeException("Test value mismatch"));
            }
            return success;

        } catch (Exception e) {
            markHealthCheckFailure(e);
            return false;
        }
    }

    /**
     * Get Redis connection info
     */
    public String getRedisConnectionInfo() {
        try {
            // Use RedisTemplate to get Redis info instead of direct connection
            return redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    Properties info = connection.info();
                    return info.getProperty("redis_version", "Unknown");
                } catch (Exception e) {
                    return "Unable to retrieve version: " + e.getMessage();
                }
            });
        } catch (Exception e) {
            return "Unable to retrieve Redis info: " + e.getMessage();
        }
    }

    /**
     * Mark a successful health check
     */
    private void markHealthCheckSuccess() {
        boolean wasUnavailable = !redisAvailable.get();
        redisAvailable.set(true);
        consecutiveFailures.set(0);
        lastSuccessfulCheck = Instant.now();
        lastErrorMessage = null;

        if (wasUnavailable) {
            log.info("Redis connectivity restored after {} consecutive failures", consecutiveFailures.get());
        } else {
            log.debug("Redis health check successful");
        }
    }

    /**
     * Mark a failed health check
     */
    private void markHealthCheckFailure(Exception e) {
        int failures = consecutiveFailures.incrementAndGet();
        lastFailedCheck = Instant.now();
        lastErrorMessage = e.getMessage();

        if (failures >= properties.getMaxConsecutiveFailures()) {
            if (redisAvailable.get()) {
                log.error("Redis marked as unavailable after {} consecutive failures. Last error: {}",
                         failures, e.getMessage());
            }
            redisAvailable.set(false);
        } else {
            log.warn("Redis health check failed (attempt {}/{}): {}",
                    failures, properties.getMaxConsecutiveFailures(), e.getMessage());
        }
    }
}
