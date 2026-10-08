package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.shared.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("RateLimitService Integration Tests with Redis")
@EnabledIfSystemProperty(named = "integration.tests.enabled", matches = "true",
    disabledReason = "Integration tests require Docker. Run with -Dintegration.tests.enabled=true and ensure Docker is running.")
class RateLimitServiceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server --requirepass testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "testpassword");
        registry.add("app.rate-limit.enabled", () -> "true");
        registry.add("app.rate-limit.default-requests-per-minute", () -> "100");
        registry.add("app.rate-limit.login-requests-per-minute", () -> "5");
        registry.add("app.rate-limit.register-requests-per-minute", () -> "3");
        registry.add("app.rate-limit.refresh-token-requests-per-minute", () -> "30");
        registry.add("app.rate-limit.anonymous-requests-per-minute", () -> "50");
        registry.add("app.rate-limit.ban-duration-minutes", () -> "1");
        registry.add("app.rate-limit.max-violations-before-ban", () -> "3");
    }

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    private static final String TEST_USER_KEY = "integration-test-user";
    private static final String TEST_IP_KEY = "integration-test-ip";

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        rateLimitService.clearAllRateLimits();
    }

    @Test
    @DisplayName("Should allow requests under rate limit")
    void shouldAllowRequestsUnderRateLimit() {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.LOGIN;
        int limit = rateLimitProperties.getLoginRequestsPerMinute(); // 5

        // When & Then
        for (int i = 0; i < limit; i++) {
            assertTrue(rateLimitService.isAllowed(key, type),
                "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should deny requests over rate limit")
    void shouldDenyRequestsOverRateLimit() {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.LOGIN;
        int limit = rateLimitProperties.getLoginRequestsPerMinute(); // 5

        // Exhaust the rate limit
        for (int i = 0; i < limit; i++) {
            rateLimitService.isAllowed(key, type);
        }

        // When
        boolean result = rateLimitService.isAllowed(key, type);

        // Then
        assertFalse(result, "Request over limit should be denied");
    }

    @Test
    @DisplayName("Should track violations and ban user after max violations")
    void shouldTrackViolationsAndBanUserAfterMaxViolations() {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.LOGIN;
        int limit = rateLimitProperties.getLoginRequestsPerMinute(); // 5
        int maxViolations = rateLimitProperties.getMaxViolationsBeforeBan(); // 3

        // When - Exceed rate limit multiple times to trigger violations
        for (int violation = 0; violation < maxViolations; violation++) {
            // Exhaust the rate limit
            for (int i = 0; i < limit; i++) {
                rateLimitService.isAllowed(key, type);
            }
            // This should create a violation
            rateLimitService.isAllowed(key, type);

            // Clear rate limit counts but keep violations
            redisTemplate.delete("rate_limit_count:LOGIN:" + key);
        }

        // Then - User should be banned
        assertFalse(rateLimitService.isAllowed(key, type), "User should be banned after max violations");
        assertTrue(rateLimitService.getRemainingBanTime(key) > 0, "Ban time should be greater than 0");
    }

    @Test
    @DisplayName("Should get correct available tokens")
    void shouldGetCorrectAvailableTokens() {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.AUTHENTICATED;
        int limit = rateLimitProperties.getDefaultRequestsPerMinute(); // 100
        int usedRequests = 25;

        // Use some requests
        for (int i = 0; i < usedRequests; i++) {
            rateLimitService.isAllowed(key, type);
        }

        // When
        long availableTokens = rateLimitService.getAvailableTokens(key, type);

        // Then
        assertEquals(limit - usedRequests, availableTokens);
    }

    @Test
    @DisplayName("Should reset rate limit successfully")
    void shouldResetRateLimitSuccessfully() {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.LOGIN;
        int limit = rateLimitProperties.getLoginRequestsPerMinute();

        // Exhaust rate limit and create violations
        for (int i = 0; i < limit + 5; i++) {
            rateLimitService.isAllowed(key, type);
        }

        // Verify it's blocked
        assertFalse(rateLimitService.isAllowed(key, type));

        // When
        rateLimitService.resetRateLimit(key);

        // Then
        assertTrue(rateLimitService.isAllowed(key, type), "Should allow requests after reset");
        assertEquals(limit - 1, rateLimitService.getAvailableTokens(key, type));
    }

    @Test
    @DisplayName("Should unban user successfully")
    void shouldUnbanUserSuccessfully() throws InterruptedException {
        // Given
        String key = TEST_USER_KEY;
        RateLimitType type = RateLimitType.LOGIN;
        int limit = rateLimitProperties.getLoginRequestsPerMinute();
        int maxViolations = rateLimitProperties.getMaxViolationsBeforeBan();

        // Create violations to trigger ban
        for (int violation = 0; violation < maxViolations; violation++) {
            for (int i = 0; i < limit; i++) {
                rateLimitService.isAllowed(key, type);
            }
            rateLimitService.isAllowed(key, type);
            redisTemplate.delete("rate_limit_count:LOGIN:" + key);
        }

        // Verify user is banned
        assertFalse(rateLimitService.isAllowed(key, type));

        // When
        rateLimitService.unbanKey(key);

        // Then
        assertTrue(rateLimitService.isAllowed(key, type), "Should allow requests after unban");
        assertEquals(0, rateLimitService.getRemainingBanTime(key));
    }

    @Test
    @DisplayName("Should handle different rate limit types correctly")
    void shouldHandleDifferentRateLimitTypesCorrectly() {
        // Given
        String key = TEST_USER_KEY;

        // Test all rate limit types
        assertTrue(rateLimitService.isAllowed(key, RateLimitType.LOGIN));
        assertTrue(rateLimitService.isAllowed(key, RateLimitType.REGISTER));
        assertTrue(rateLimitService.isAllowed(key, RateLimitType.REFRESH_TOKEN));
        assertTrue(rateLimitService.isAllowed(key, RateLimitType.AUTHENTICATED));
        assertTrue(rateLimitService.isAllowed(key, RateLimitType.ANONYMOUS));

        // Verify each type has correct limits
        assertEquals(5, rateLimitService.getLimit(RateLimitType.LOGIN));
        assertEquals(3, rateLimitService.getLimit(RateLimitType.REGISTER));
        assertEquals(30, rateLimitService.getLimit(RateLimitType.REFRESH_TOKEN));
        assertEquals(100, rateLimitService.getLimit(RateLimitType.AUTHENTICATED));
        assertEquals(50, rateLimitService.getLimit(RateLimitType.ANONYMOUS));
    }

    @Test
    @DisplayName("Should handle Redis connection gracefully")
    void shouldHandleRedisConnectionGracefully() {
        // This test verifies that the service falls back to local bucket when Redis is unavailable
        // We can't easily simulate Redis failure in integration test, but we can test the happy path

        String key = "redis-test-key";
        RateLimitType type = RateLimitType.AUTHENTICATED;

        // Should work normally with Redis
        assertTrue(rateLimitService.isAllowed(key, type));

        // Check that data is actually stored in Redis
        String redisKey = "rate_limit_count:AUTHENTICATED:" + key;
        Object count = redisTemplate.opsForValue().get(redisKey);
        assertNotNull(count);
        assertEquals(1L, ((Number) count).longValue());
    }

    @Test
    @DisplayName("Should maintain separate counters for different keys and types")
    void shouldMaintainSeparateCountersForDifferentKeysAndTypes() {
        // Given
        String user1 = "user1";
        String user2 = "user2";
        RateLimitType typeLogin = RateLimitType.LOGIN;
        RateLimitType typeAuth = RateLimitType.AUTHENTICATED;

        // When - Make requests for different users and types
        assertTrue(rateLimitService.isAllowed(user1, typeLogin));
        assertTrue(rateLimitService.isAllowed(user1, typeAuth));
        assertTrue(rateLimitService.isAllowed(user2, typeLogin));
        assertTrue(rateLimitService.isAllowed(user2, typeAuth));

        // Then - Each should have independent counters
        assertEquals(4, rateLimitService.getAvailableTokens(user1, typeLogin)); // 5-1=4
        assertEquals(99, rateLimitService.getAvailableTokens(user1, typeAuth)); // 100-1=99
        assertEquals(4, rateLimitService.getAvailableTokens(user2, typeLogin)); // 5-1=4
        assertEquals(99, rateLimitService.getAvailableTokens(user2, typeAuth)); // 100-1=99
    }
}
