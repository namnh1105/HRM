package com.hainam.worksphere.shared.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallback cache manager that gracefully handles Redis connection failures
 * by falling back to database queries when Redis is unavailable.
 */
@Slf4j
public class FallbackCacheManager implements CacheManager {

    private final RedisCacheManager redisCacheManager;
    private final boolean enableFallback;
    private final Map<String, Cache> cacheInstances = new ConcurrentHashMap<>();

    public FallbackCacheManager(RedisCacheManager redisCacheManager, boolean enableFallback) {
        this.redisCacheManager = redisCacheManager;
        this.enableFallback = enableFallback;
    }

    @Override
    public Cache getCache(String name) {
        if (!enableFallback) {
            return redisCacheManager.getCache(name);
        }

        // Cache wrapper instances by name so startup and runtime don't repeatedly re-create/cache-check each cache.
        return cacheInstances.computeIfAbsent(name, this::createCacheWithFallback);
    }

    @Override
    public Collection<String> getCacheNames() {
        try {
            return redisCacheManager.getCacheNames();
        } catch (Exception e) {
            log.warn("Unable to retrieve cache names from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Cache createCacheWithFallback(String name) {
        try {
            Cache redisCache = redisCacheManager.getCache(name);
            if (redisCache != null) {
                return new FallbackCache(redisCache, name);
            }
            return new NoOpCache(name);
        } catch (Exception e) {
            log.warn("Redis cache '{}' unavailable, using no-op cache. Error: {}", name, e.getMessage());
            return new NoOpCache(name);
        }
    }
}
