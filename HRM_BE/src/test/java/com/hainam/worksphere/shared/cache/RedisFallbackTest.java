package com.hainam.worksphere.shared.cache;

import com.hainam.worksphere.shared.cache.FallbackCache;
import com.hainam.worksphere.shared.cache.FallbackCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;
import org.springframework.data.redis.cache.RedisCacheManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for Redis cache fallback functionality
 */
public class RedisFallbackTest {

    private RedisCacheManager mockRedisCacheManager;
    private FallbackCacheManager fallbackCacheManager;

    @BeforeEach
    void setUp() {
        mockRedisCacheManager = mock(RedisCacheManager.class);
        fallbackCacheManager = new FallbackCacheManager(mockRedisCacheManager, true);
    }

    @Test
    public void testFallbackWhenRedisUnavailable() {
        // Given: Redis manager returns null (unavailable)
        when(mockRedisCacheManager.getCache("testCache")).thenReturn(null);

        // When: Get cache from fallback manager
        Cache cache = fallbackCacheManager.getCache("testCache");

        // Then: Should return NoOpCache
        assertNotNull(cache);
        assertTrue(cache instanceof NoOpCache);
        assertEquals("testCache", cache.getName());
    }

    @Test
    public void testFallbackDisabledUsesRedisCacheManager() {
        // Given: Fallback disabled
        FallbackCacheManager disabledFallback = new FallbackCacheManager(mockRedisCacheManager, false);
        Cache mockCache = mock(Cache.class);
        when(mockRedisCacheManager.getCache("testCache")).thenReturn(mockCache);

        // When: Get cache
        Cache result = disabledFallback.getCache("testCache");

        // Then: Should return the Redis cache directly
        assertEquals(mockCache, result);
        verify(mockRedisCacheManager).getCache("testCache");
    }

    @Test
    public void testFallbackCacheOperations() {
        // Given: A NoOpCache (simulating Redis unavailable)
        Cache noOpCache = new NoOpCache("testCache");

        // When & Then: All operations should work without throwing exceptions
        assertDoesNotThrow(() -> {
            // Get operations
            assertNull(noOpCache.get("key"));
            assertNull(noOpCache.get("key", String.class));
            
            // Put operations
            noOpCache.put("key", "value");
            assertNull(noOpCache.putIfAbsent("key", "value"));
            
            // Evict operations
            noOpCache.evict("key");
            assertFalse(noOpCache.evictIfPresent("key"));
            
            // Clear operation
            noOpCache.clear();
        });
    }

    @Test
    public void testCacheNameRetrieval() {
        // Given: Redis manager with cache names
        when(mockRedisCacheManager.getCacheNames()).thenReturn(java.util.Set.of("cache1", "cache2"));

        // When: Get cache names
        var cacheNames = fallbackCacheManager.getCacheNames();

        // Then: Should return the cache names
        assertEquals(2, cacheNames.size());
        assertTrue(cacheNames.contains("cache1"));
        assertTrue(cacheNames.contains("cache2"));
    }
}
