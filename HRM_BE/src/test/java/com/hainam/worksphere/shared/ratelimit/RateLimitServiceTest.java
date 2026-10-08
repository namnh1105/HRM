package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.shared.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitService Unit Tests")
class RateLimitServiceTest extends BaseUnitTest {

    @Mock
    private RedisRateLimitOperations redisRateLimitOperations;

    @Mock
    private RateLimitProperties rateLimitProperties;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        // Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(rateLimitProperties.isEnabled()).thenReturn(true);
        lenient().when(rateLimitProperties.getDefaultRequestsPerMinute()).thenReturn(100);
        lenient().when(rateLimitProperties.getLoginRequestsPerMinute()).thenReturn(10);
        lenient().when(rateLimitProperties.getRegisterRequestsPerMinute()).thenReturn(5);
        lenient().when(rateLimitProperties.getRefreshTokenRequestsPerMinute()).thenReturn(30);
        lenient().when(rateLimitProperties.getAnonymousRequestsPerMinute()).thenReturn(50);
        lenient().when(rateLimitProperties.getBanDurationMinutes()).thenReturn(15);
        lenient().when(rateLimitProperties.getMaxViolationsBeforeBan()).thenReturn(5);

        // Mock Redis availability
        lenient().when(redisRateLimitOperations.isRedisAvailable()).thenReturn(true);

        rateLimitService = new RateLimitService(rateLimitProperties, redisRateLimitOperations);
    }

    @Test
    @DisplayName("Should allow request when under rate limit")
    void shouldAllowRequestWhenUnderRateLimit() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTHENTICATED;
        String redisKey = "rate_limit_count:AUTHENTICATED:test-user";

        when(redisRateLimitOperations.increment(redisKey, 1)).thenReturn(1L); // First request
        when(redisRateLimitOperations.hasKey("rate_limit_ban:test-user")).thenReturn(false);
        when(redisRateLimitOperations.expire(redisKey, 60, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = rateLimitService.isAllowed(key, type);

        // Then
        assertTrue(result);
        verify(redisRateLimitOperations).increment(redisKey, 1);
        verify(redisRateLimitOperations).expire(redisKey, 60, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should deny request when rate limit exceeded")
    void shouldDenyRequestWhenRateLimitExceeded() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.LOGIN;
        String redisKey = "rate_limit_count:LOGIN:test-user";

        when(redisRateLimitOperations.increment(redisKey, 1)).thenReturn(15L);
        when(redisRateLimitOperations.hasKey("rate_limit_ban:test-user")).thenReturn(false);

        // When
        boolean result = rateLimitService.isAllowed(key, type);

        // Then
        assertFalse(result);
        verify(redisRateLimitOperations).increment("rate_limit_violation:test-user", 1);
    }

    @Test
    @DisplayName("Should deny request when user is banned")
    void shouldDenyRequestWhenUserIsBanned() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTHENTICATED;

        when(redisRateLimitOperations.hasKey("rate_limit_ban:test-user")).thenReturn(true);

        // When
        boolean result = rateLimitService.isAllowed(key, type);

        // Then
        assertFalse(result);
        verify(redisRateLimitOperations).hasKey("rate_limit_ban:test-user");
        // Should not increment count when banned
        verify(redisRateLimitOperations, never()).increment(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should ban user after max violations")
    void shouldBanUserAfterMaxViolations() {
        // Given
        String key = "test-user";
        String redisKey = "rate_limit_count:LOGIN:test-user";
        String violationKey = "rate_limit_violation:test-user";
        String banKey = "rate_limit_ban:test-user";

        // Mock rate limit exceeded
        when(redisRateLimitOperations.increment(redisKey, 1)).thenReturn(15L); // Exceeds limit of 10
        when(redisRateLimitOperations.hasKey(banKey)).thenReturn(false);
        when(redisRateLimitOperations.increment(violationKey, 1)).thenReturn(5L); // Max violations reached

        // When
        boolean result = rateLimitService.isAllowed(key, RateLimitType.LOGIN);

        // Then
        assertFalse(result);
        verify(redisRateLimitOperations).increment(violationKey, 1);
        verify(redisRateLimitOperations).set(eq(banKey), eq("banned"), any(Duration.class));
        verify(redisRateLimitOperations).delete(violationKey);
    }

    @Test
    @DisplayName("Should get correct limit for each rate limit type")
    void shouldGetCorrectLimitForEachType() {
        // Test all rate limit types
        assertEquals(100, rateLimitService.getLimit(RateLimitType.AUTHENTICATED));
        assertEquals(10, rateLimitService.getLimit(RateLimitType.LOGIN));
        assertEquals(5, rateLimitService.getLimit(RateLimitType.REGISTER));
        assertEquals(30, rateLimitService.getLimit(RateLimitType.REFRESH_TOKEN));
        assertEquals(50, rateLimitService.getLimit(RateLimitType.ANONYMOUS));
    }

    @Test
    @DisplayName("Should get available tokens correctly")
    void shouldGetAvailableTokensCorrectly() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTHENTICATED;
        String redisKey = "rate_limit_count:AUTHENTICATED:test-user";

        when(redisRateLimitOperations.get(redisKey)).thenReturn(25L);

        // When
        long availableTokens = rateLimitService.getAvailableTokens(key, type);

        // Then
        assertEquals(75, availableTokens); // 100 - 25 = 75
    }

    @Test
    @DisplayName("Should return full limit when no usage recorded")
    void shouldReturnFullLimitWhenNoUsageRecorded() {
        // Given
        String key = "new-user";
        RateLimitType type = RateLimitType.AUTHENTICATED;
        String redisKey = "rate_limit_count:AUTHENTICATED:new-user";

        when(redisRateLimitOperations.get(redisKey)).thenReturn(null);

        // When
        long availableTokens = rateLimitService.getAvailableTokens(key, type);

        // Then
        assertEquals(100, availableTokens);
    }

    @Test
    @DisplayName("Should get remaining ban time")
    void shouldGetRemainingBanTime() {
        // Given
        String key = "banned-user";
        String banKey = "rate_limit_ban:banned-user";

        when(redisRateLimitOperations.getExpire(banKey, TimeUnit.SECONDS)).thenReturn(300L); // 5 minutes

        // When
        long remainingTime = rateLimitService.getRemainingBanTime(key);

        // Then
        assertEquals(300, remainingTime);
    }

    @Test
    @DisplayName("Should reset rate limit for user")
    void shouldResetRateLimitForUser() {
        // Given
        String key = "test-user";

        // When
        rateLimitService.resetRateLimit(key);

        // Then
        verify(redisRateLimitOperations).delete("rate_limit_ban:test-user");
        verify(redisRateLimitOperations).delete("rate_limit_violation:test-user");
        verify(redisRateLimitOperations, times(7)).delete(anyString()); // Ban + Violation + 5 rate limit types = 7 total
    }

    @Test
    @DisplayName("Should unban user")
    void shouldUnbanUser() {
        // Given
        String key = "banned-user";

        // When
        rateLimitService.unbanKey(key);

        // Then
        verify(redisRateLimitOperations).delete("rate_limit_ban:banned-user");
    }

    @Test
    @DisplayName("Should allow all requests when rate limiting is disabled")
    void shouldAllowAllRequestsWhenRateLimitingDisabled() {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(false);
        rateLimitService = new RateLimitService(rateLimitProperties, redisRateLimitOperations);

        // When
        boolean result = rateLimitService.isAllowed("any-key", RateLimitType.LOGIN);

        // Then
        assertTrue(result);
        verify(redisRateLimitOperations, never()).increment(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should fallback to local bucket when Redis fails")
    void shouldFallbackToLocalBucketWhenRedisFails() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTHENTICATED;

        when(redisRateLimitOperations.hasKey(anyString())).thenReturn(false);
        when(redisRateLimitOperations.increment(anyString(), anyLong())).thenReturn(null); // Redis unavailable

        // When
        boolean result = rateLimitService.isAllowed(key, type);

        // Then
        // Should not throw exception and fallback to local bucket
        assertNotNull(result);
        // Should still be true for first request even with Redis failure
        assertTrue(result);
    }
}
