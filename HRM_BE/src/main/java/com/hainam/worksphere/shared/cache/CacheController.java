package com.hainam.worksphere.shared.cache;

import com.hainam.worksphere.shared.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Admin controller for cache management operations
 */
@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Optional<RedisHealthCheckService> redisHealthCheckService;
    private final Optional<RateLimitService> rateLimitService;

    /**
     * Get all cache names
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getCacheNames() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        return ResponseEntity.ok(Map.of(
            "cacheNames", cacheNames,
            "count", cacheNames.size()
        ));
    }

    /**
     * Clear a specific cache
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache cleared: {}", cacheName);
            return ResponseEntity.ok(Map.of(
                "message", "Cache cleared successfully",
                "cacheName", cacheName
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cache not found",
                "cacheName", cacheName
            ));
        }
    }

    /**
     * Clear all caches
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        List<String> clearedCaches = new ArrayList<>();

        for (String cacheName : cacheNames) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                clearedCaches.add(cacheName);
            }
        }

        log.warn("All caches cleared by admin");
        return ResponseEntity.ok(Map.of(
            "message", "All caches cleared successfully",
            "clearedCaches", clearedCaches,
            "count", clearedCaches.size()
        ));
    }

    /**
     * Evict a specific key from a cache
     */
    @DeleteMapping("/{cacheName}/key/{key}")
    public ResponseEntity<Map<String, String>> evictKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Key evicted from cache: {} -> {}", cacheName, key);
            return ResponseEntity.ok(Map.of(
                "message", "Key evicted successfully",
                "cacheName", cacheName,
                "key", key
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cache not found",
                "cacheName", cacheName
            ));
        }
    }

    /**
     * Get cache statistics (Redis keys count)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();

        for (String cacheName : cacheNames) {
            try {
                Set<String> keys = redisTemplate.keys(cacheName + "::*");
                stats.put(cacheName, keys != null ? keys.size() : 0);
            } catch (Exception e) {
                stats.put(cacheName, "Error: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
            "cacheStats", stats,
            "totalCaches", cacheNames.size()
        ));
    }

    /**
     * Get Redis health status and fallback information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        Map<String, Object> health = new HashMap<>();

        if (redisHealthCheckService.isPresent()) {
            RedisHealthCheckService service = redisHealthCheckService.get();
            health.put("redisAvailable", service.isRedisAvailable());
            health.put("consecutiveFailures", service.getConsecutiveFailures());
            health.put("lastSuccessfulCheck", service.getLastSuccessfulCheck());
            health.put("lastFailedCheck", service.getLastFailedCheck());
            health.put("lastErrorMessage", service.getLastErrorMessage());
            health.put("connectionInfo", service.getRedisConnectionInfo());
        } else {
            health.put("healthCheckEnabled", false);
            health.put("message", "Redis health check service not available");
        }

        // Add rate limiting Redis status
        if (rateLimitService.isPresent()) {
            RateLimitService rateService = rateLimitService.get();
            health.put("rateLimitRedisAvailable", rateService.isRedisAvailable());
        } else {
            health.put("rateLimitRedisAvailable", "N/A");
        }

        health.put("cacheManagerType", cacheManager.getClass().getSimpleName());
        health.put("fallbackEnabled", cacheManager instanceof FallbackCacheManager);
        health.put("timestamp", Instant.now());

        return ResponseEntity.ok(health);
    }

    /**
     * Manually test Redis connectivity
     */
    @PostMapping("/test-redis")
    public ResponseEntity<Map<String, Object>> testRedisConnectivity() {
        Map<String, Object> result = new HashMap<>();

        if (redisHealthCheckService.isPresent()) {
            boolean cacheRedisAvailable = redisHealthCheckService.get().testRedisConnectivity();
            result.put("cacheRedisAvailable", cacheRedisAvailable);
        } else {
            result.put("cacheRedisAvailable", "N/A");
        }

        if (rateLimitService.isPresent()) {
            boolean rateLimitRedisAvailable = rateLimitService.get().testRedisConnectivity();
            result.put("rateLimitRedisAvailable", rateLimitRedisAvailable);
        } else {
            result.put("rateLimitRedisAvailable", "N/A");
        }

        result.put("testPerformed", true);
        result.put("timestamp", Instant.now());

        boolean allAvailable = (boolean) result.getOrDefault("cacheRedisAvailable", true) &&
                              (boolean) result.getOrDefault("rateLimitRedisAvailable", true);
        result.put("message", allAvailable ? "All Redis connectivity tests passed" : "Some Redis connectivity tests failed");

        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed cache configuration information
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getCacheConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put("cacheManagerType", cacheManager.getClass().getSimpleName());
        config.put("cacheNames", cacheManager.getCacheNames());
        config.put("fallbackEnabled", cacheManager instanceof FallbackCacheManager);

        if (redisHealthCheckService.isPresent()) {
            config.put("healthCheckEnabled", true);
            config.put("redisStatus", redisHealthCheckService.get().isRedisAvailable() ? "AVAILABLE" : "UNAVAILABLE");
        } else {
            config.put("healthCheckEnabled", false);
            config.put("redisStatus", "UNKNOWN");
        }

        config.put("timestamp", Instant.now());

        return ResponseEntity.ok(config);
    }
}

