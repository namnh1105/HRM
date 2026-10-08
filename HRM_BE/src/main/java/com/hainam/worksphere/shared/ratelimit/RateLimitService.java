package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.shared.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RateLimitProperties rateLimitProperties;
    private final RedisRateLimitOperations redisOperations;

    private final Map<String, Bucket> localBucketCache = new ConcurrentHashMap<>();

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String VIOLATION_KEY_PREFIX = "rate_limit_violation:";
    private static final String BAN_KEY_PREFIX = "rate_limit_ban:";
    private static final String REQUEST_COUNT_PREFIX = "rate_limit_count:";

    public boolean isAllowed(String key, RateLimitType type) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        if (isBanned(key)) {
            log.warn("Rate limit: Key {} is currently banned", key);
            return false;
        }

        boolean allowed = isAllowedByRedis(key, type);

        if (!allowed) {
            recordViolation(key);
            log.warn("Rate limit exceeded for key: {} type: {}", key, type);
        }

        return allowed;
    }

    private boolean isAllowedByRedis(String key, RateLimitType type) {
        String redisKey = REQUEST_COUNT_PREFIX + type.name() + ":" + key;
        int limit = getLimit(type);
        long windowSizeInSeconds = 60;

        // Try Redis operations with fallback
        Long currentCount = redisOperations.increment(redisKey, 1);

        if (currentCount == null) {
            log.warn("Redis unavailable for rate limiting, falling back to local bucket for key: {}", key);
            return isAllowedByLocalBucket(key, type);
        }

        if (currentCount == 1) {
            redisOperations.expire(redisKey, windowSizeInSeconds, TimeUnit.SECONDS);
        }

        return currentCount <= limit;
    }

    private boolean isAllowedByLocalBucket(String key, RateLimitType type) {
        Bucket bucket = getBucket(key, type);
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String key, RateLimitType type) {
        String redisKey = REQUEST_COUNT_PREFIX + type.name() + ":" + key;
        int limit = getLimit(type);

        Object countObj = redisOperations.get(redisKey);
        if (countObj == null) {
            // Redis unavailable or key doesn't exist, fall back to local bucket
            if (!redisOperations.isRedisAvailable()) {
                Bucket bucket = getBucket(key, type);
                return bucket.getAvailableTokens();
            }
            return limit;
        }

        try {
            long currentCount = ((Number) countObj).longValue();
            return Math.max(0, limit - currentCount);
        } catch (Exception e) {
            log.error("Failed to parse token count from Redis for key: {}", key, e);
            Bucket bucket = getBucket(key, type);
            return bucket.getAvailableTokens();
        }
    }

    public int getLimit(RateLimitType type) {
        return switch (type) {
            case LOGIN -> rateLimitProperties.getLoginRequestsPerMinute();
            case REGISTER -> rateLimitProperties.getRegisterRequestsPerMinute();
            case REFRESH_TOKEN -> rateLimitProperties.getRefreshTokenRequestsPerMinute();
            case ANONYMOUS -> rateLimitProperties.getAnonymousRequestsPerMinute();
            case AUTHENTICATED -> rateLimitProperties.getDefaultRequestsPerMinute();
        };
    }

    private Bucket getBucket(String key, RateLimitType type) {
        String cacheKey = RATE_LIMIT_KEY_PREFIX + type.name() + ":" + key;
        return localBucketCache.computeIfAbsent(cacheKey, k -> createBucket(type));
    }

    private Bucket createBucket(RateLimitType type) {
        int requestsPerMinute = getLimit(type);

        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))
        );

        return Bucket.builder().addLimit(limit).build();
    }

    private void recordViolation(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;

        Long violations = redisOperations.increment(violationKey, 1);

        if (violations == null) {
            // Redis unavailable, log but don't fail
            log.warn("Unable to record rate limit violation for key: {} (Redis unavailable)", key);
            return;
        }

        if (violations == 1) {
            redisOperations.expire(violationKey, 1, TimeUnit.HOURS);
        }

        if (violations >= rateLimitProperties.getMaxViolationsBeforeBan()) {
            banKey(key);
            redisOperations.delete(violationKey);
        }
    }

    private void banKey(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        Duration banDuration = Duration.ofMinutes(rateLimitProperties.getBanDurationMinutes());
        redisOperations.set(banKey, "banned", banDuration);
        log.warn("Rate limit: Key {} has been banned for {} minutes", key, rateLimitProperties.getBanDurationMinutes());
    }

    private boolean isBanned(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        Boolean banned = redisOperations.hasKey(banKey);
        return Boolean.TRUE.equals(banned);
    }

    public void resetRateLimit(String key) {
        localBucketCache.entrySet().removeIf(entry -> entry.getKey().contains(key));

        redisOperations.delete(BAN_KEY_PREFIX + key);
        redisOperations.delete(VIOLATION_KEY_PREFIX + key);

        for (RateLimitType type : RateLimitType.values()) {
            redisOperations.delete(REQUEST_COUNT_PREFIX + type.name() + ":" + key);
        }

        log.info("Rate limit reset for key: {}", key);
    }

    public void clearAllRateLimits() {
        localBucketCache.clear();

        if (!redisOperations.isRedisAvailable()) {
            log.warn("Cannot clear Redis rate limits - Redis unavailable. Local caches cleared.");
            return;
        }

        // Note: We'll keep this simple for now since RedisRateLimitOperations doesn't have keys() operation
        // This method is typically used for admin operations, so it's acceptable to have limited functionality
        // when Redis is partially available
        log.warn("Clear all rate limits partially implemented - local caches cleared. Redis keys require manual cleanup.");
    }

    public long getRemainingBanTime(String key) {
        if (!redisOperations.isRedisAvailable()) {
            return 0; // Assume no ban time if Redis is unavailable
        }

        String banKey = BAN_KEY_PREFIX + key;
        Long remainingTime = redisOperations.getExpire(banKey, TimeUnit.SECONDS);
        return remainingTime != null && remainingTime > 0 ? remainingTime : 0;
    }

    /**
     * Check if Redis is available for rate limiting operations
     */
    public boolean isRedisAvailable() {
        return redisOperations.isRedisAvailable();
    }

    /**
     * Test Redis connectivity for rate limiting
     */
    public boolean testRedisConnectivity() {
        return redisOperations.testConnectivity();
    }

    public void unbanKey(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        redisOperations.delete(banKey);
        log.info("Rate limit ban removed for key: {}", key);
    }
}
