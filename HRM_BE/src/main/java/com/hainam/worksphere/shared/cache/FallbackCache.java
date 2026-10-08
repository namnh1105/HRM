package com.hainam.worksphere.shared.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.concurrent.Callable;

/**
 * Cache wrapper that provides fallback behavior when Redis operations fail.
 * When Redis is unavailable, it gracefully degrades to no-op behavior,
 * allowing the application to continue functioning with database queries.
 */
@Slf4j
public class FallbackCache implements Cache {

    private final Cache redisCache;
    private final String name;
    private volatile boolean redisAvailable = true;

    public FallbackCache(Cache redisCache, String name) {
        this.redisCache = redisCache;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return redisCache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping get operation for key: {}", name, key);
            return null;
        }

        try {
            ValueWrapper result = redisCache.get(key);
            markRedisAvailable();
            return result;
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("get", key, e);
            return null;
        } catch (Exception e) {
            log.warn("Unexpected error during cache get operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping get operation for key: {}", name, key);
            return null;
        }

        try {
            T result = redisCache.get(key, type);
            markRedisAvailable();
            return result;
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("get", key, e);
            return null;
        } catch (Exception e) {
            log.warn("Unexpected error during cache get operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', executing value loader directly for key: {}", name, key);
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new Cache.ValueRetrievalException(key, valueLoader, e);
            }
        }

        try {
            T result = redisCache.get(key, valueLoader);
            markRedisAvailable();
            return result;
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("get with loader", key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new Cache.ValueRetrievalException(key, valueLoader, ex);
            }
        } catch (Exception e) {
            log.warn("Unexpected error during cache get operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new Cache.ValueRetrievalException(key, valueLoader, ex);
            }
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping put operation for key: {}", name, key);
            return;
        }

        try {
            redisCache.put(key, value);
            markRedisAvailable();
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("put", key, e);
        } catch (Exception e) {
            log.warn("Unexpected error during cache put operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping putIfAbsent operation for key: {}", name, key);
            return null;
        }

        try {
            ValueWrapper result = redisCache.putIfAbsent(key, value);
            markRedisAvailable();
            return result;
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("putIfAbsent", key, e);
            return null;
        } catch (Exception e) {
            log.warn("Unexpected error during cache putIfAbsent operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping evict operation for key: {}", name, key);
            return;
        }

        try {
            redisCache.evict(key);
            markRedisAvailable();
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("evict", key, e);
        } catch (Exception e) {
            log.warn("Unexpected error during cache evict operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
        }
    }

    @Override
    public boolean evictIfPresent(Object key) {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping evictIfPresent operation for key: {}", name, key);
            return false;
        }

        try {
            boolean result = redisCache.evictIfPresent(key);
            markRedisAvailable();
            return result;
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("evictIfPresent", key, e);
            return false;
        } catch (Exception e) {
            log.warn("Unexpected error during cache evictIfPresent operation for key '{}' in cache '{}': {}", key, name, e.getMessage());
            return false;
        }
    }

    @Override
    public void clear() {
        if (!redisAvailable) {
            log.trace("Redis unavailable for cache '{}', skipping clear operation", name);
            return;
        }

        try {
            redisCache.clear();
            markRedisAvailable();
        } catch (RedisConnectionFailureException e) {
            handleRedisFailure("clear", "all", e);
        } catch (Exception e) {
            log.warn("Unexpected error during cache clear operation for cache '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Handle Redis connection failures
     */
    private void handleRedisFailure(String operation, Object key, Exception e) {
        redisAvailable = false;
        log.warn("Redis connection failed during '{}' operation for key '{}' in cache '{}'. " +
                "Falling back to database queries. Error: {}", operation, key, name, e.getMessage());

        // Schedule a check to see if Redis becomes available again
        scheduleRedisReconnectCheck();
    }

    /**
     * Mark Redis as available
     */
    private void markRedisAvailable() {
        if (!redisAvailable) {
            log.info("Redis connection restored for cache '{}'", name);
            redisAvailable = true;
        }
    }

    /**
     * Schedule a check to see if Redis becomes available again
     * This is a simple implementation - in production, you might want to use a more sophisticated approach
     */
    private void scheduleRedisReconnectCheck() {
        // For now, we rely on the next cache operation to test connectivity
        // In a production environment, you might want to implement a background task
        // to periodically check Redis connectivity and update the redisAvailable flag
    }
}
