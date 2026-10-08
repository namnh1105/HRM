package com.hainam.worksphere.shared.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis operations wrapper that handles connection failures gracefully for rate limiting
 */
@Component
@Slf4j
public class RedisRateLimitOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private volatile boolean redisAvailable = true;

    public RedisRateLimitOperations(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Increment a key in Redis with fallback behavior
     */
    public Long increment(String key, long delta) {
        if (!redisAvailable) {
            return null;
        }

        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("increment", key, e);
            return null;
        }
    }

    /**
     * Set expiration on a key with fallback behavior
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (!redisAvailable) {
            return false;
        }

        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("expire", key, e);
            return false;
        }
    }

    /**
     * Get a value from Redis with fallback behavior
     */
    public Object get(String key) {
        if (!redisAvailable) {
            return null;
        }

        try {
            Object result = redisTemplate.opsForValue().get(key);
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("get", key, e);
            return null;
        }
    }

    /**
     * Set a value in Redis with TTL and fallback behavior
     */
    public void set(String key, Object value, Duration timeout) {
        if (!redisAvailable) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, value, timeout);
            markRedisAvailable();
        } catch (Exception e) {
            handleRedisFailure("set", key, e);
        }
    }

    /**
     * Set a value in Redis with TTL and fallback behavior (alternative method name for test compatibility)
     */
    public void setWithExpiration(String key, Object value, Duration timeout) {
        set(key, value, timeout);
    }

    /**
     * Get the expiration time of a key in Redis
     */
    public Long getExpire(String key, TimeUnit unit) {
        if (!redisAvailable) {
            return -1L;
        }

        try {
            Long result = redisTemplate.getExpire(key, unit);
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("getExpire", key, e);
            return -1L;
        }
    }

    /**
     * Check if a key exists in Redis with fallback behavior
     */
    public Boolean hasKey(String key) {
        if (!redisAvailable) {
            return false; // Assume key doesn't exist if Redis is down
        }

        try {
            Boolean result = redisTemplate.hasKey(key);
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("hasKey", key, e);
            return false; // Assume key doesn't exist if Redis fails
        }
    }

    /**
     * Delete keys from Redis with fallback behavior
     */
    public Boolean delete(String... keys) {
        if (!redisAvailable) {
            return false;
        }

        try {
            Boolean result = redisTemplate.delete(java.util.List.of(keys)) > 0;
            markRedisAvailable();
            return result;
        } catch (Exception e) {
            handleRedisFailure("delete", String.join(",", keys), e);
            return false;
        }
    }

    /**
     * Check if Redis is currently available
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /**
     * Test Redis connectivity
     */
    public boolean testConnectivity() {
        try {
            String testKey = "rate_limit:health_check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", Duration.ofSeconds(5));
            Object result = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean success = "test".equals(result);
            if (success) {
                markRedisAvailable();
            } else {
                markRedisUnavailable("Test connectivity failed: value mismatch");
            }
            return success;
        } catch (Exception e) {
            markRedisUnavailable("Test connectivity failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle Redis operation failures
     */
    private void handleRedisFailure(String operation, String key, Exception e) {
        markRedisUnavailable("Redis operation '" + operation + "' failed for key '" + key + "': " + e.getMessage());
    }

    /**
     * Mark Redis as unavailable
     */
    private void markRedisUnavailable(String reason) {
        if (redisAvailable) {
            log.warn("Redis marked as unavailable for rate limiting: {}", reason);
            redisAvailable = false;
        }
    }

    /**
     * Mark Redis as available
     */
    private void markRedisAvailable() {
        if (!redisAvailable) {
            log.info("Redis connectivity restored for rate limiting");
            redisAvailable = true;
        }
    }
}
