package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.shared.config.RateLimitProperties;
import com.hainam.worksphere.shared.exception.RateLimitBannedException;
import com.hainam.worksphere.shared.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitFilter Unit Tests")
class RateLimitFilterTest extends BaseUnitTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @Mock
    private org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService, rateLimitProperties, handlerExceptionResolver);

        // Setup default request
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
    }

    @Test
    @DisplayName("Should pass through when rate limiting is disabled")
    void shouldPassThroughWhenRateLimitingDisabled() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(false);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).isAllowed(any(), any());
    }

    @Test
    @DisplayName("Should allow request when rate limit not exceeded")
    void shouldAllowRequestWhenRateLimitNotExceeded() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.ANONYMOUS))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.ANONYMOUS)))
                .thenReturn(45L);
        when(rateLimitService.getLimit(eq(RateLimitType.ANONYMOUS))).thenReturn(50);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Limit", "50");
        verify(response).setHeader("X-RateLimit-Remaining", "45");
        verify(response).setHeader("X-RateLimit-Reset", "60");
    }

    @Test
    @DisplayName("Should resolve RateLimitExceededException when rate limit exceeded")
    void shouldResolveRateLimitExceededExceptionWhenRateLimitExceeded() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.ANONYMOUS))).thenReturn(false);
        when(rateLimitService.getRemainingBanTime(eq("ip:" + TEST_IP_ADDRESS))).thenReturn(0L);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), any(), any(RateLimitExceededException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should resolve RateLimitBannedException when user is banned")
    void shouldResolveRateLimitBannedExceptionWhenUserIsBanned() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.ANONYMOUS))).thenReturn(false);
        when(rateLimitService.getRemainingBanTime(eq("ip:" + TEST_IP_ADDRESS))).thenReturn(300L);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), any(), any(RateLimitBannedException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use authenticated user key when user is logged in")
    void shouldUseAuthenticatedUserKeyWhenUserIsLoggedIn() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(authentication.getPrincipal()).thenReturn(TEST_EMAIL);

        when(rateLimitService.isAllowed(eq("user:" + TEST_EMAIL), eq(RateLimitType.AUTHENTICATED))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("user:" + TEST_EMAIL), eq(RateLimitType.AUTHENTICATED)))
                .thenReturn(95L);
        when(rateLimitService.getLimit(eq(RateLimitType.AUTHENTICATED))).thenReturn(100);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(rateLimitService).isAllowed("user:" + TEST_EMAIL, RateLimitType.AUTHENTICATED);
    }

    @Test
    @DisplayName("Should detect login endpoint and use LOGIN rate limit type")
    void shouldDetectLoginEndpointAndUseLoginRateLimitType() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.LOGIN))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.LOGIN))).thenReturn(8L);
        when(rateLimitService.getLimit(eq(RateLimitType.LOGIN))).thenReturn(10);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("ip:" + TEST_IP_ADDRESS, RateLimitType.LOGIN);
        verify(response).setHeader("X-RateLimit-Limit", "10");
    }

    @Test
    @DisplayName("Should detect register endpoint and use REGISTER rate limit type")
    void shouldDetectRegisterEndpointAndUseRegisterRateLimitType() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.REGISTER))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.REGISTER)))
                .thenReturn(3L);
        when(rateLimitService.getLimit(eq(RateLimitType.REGISTER))).thenReturn(5);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("ip:" + TEST_IP_ADDRESS, RateLimitType.REGISTER);
        verify(response).setHeader("X-RateLimit-Limit", "5");
    }

    @Test
    @DisplayName("Should detect refresh token endpoint and use REFRESH_TOKEN rate limit type")
    void shouldDetectRefreshTokenEndpointAndUseRefreshTokenRateLimitType() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        when(rateLimitService.isAllowed(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.REFRESH_TOKEN))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:" + TEST_IP_ADDRESS), eq(RateLimitType.REFRESH_TOKEN)))
                .thenReturn(25L);
        when(rateLimitService.getLimit(eq(RateLimitType.REFRESH_TOKEN))).thenReturn(30);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("ip:" + TEST_IP_ADDRESS, RateLimitType.REFRESH_TOKEN);
        verify(response).setHeader("X-RateLimit-Limit", "30");
    }

    @Test
    @DisplayName("Should skip filter for static resources")
    void shouldSkipFilterForStaticResources() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/static/css/style.css");

        // When
        boolean shouldNotFilter = rateLimitFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Should skip filter for actuator health endpoint")
    void shouldSkipFilterForActuatorHealthEndpoint() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        boolean shouldNotFilter = rateLimitFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Should skip filter for Swagger UI")
    void shouldSkipFilterForSwaggerUI() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // When
        boolean shouldNotFilter = rateLimitFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIPFromXForwardedForHeader() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 70.41.3.18, 150.172.238.178");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        when(rateLimitService.isAllowed(eq("ip:203.0.113.1"), eq(RateLimitType.ANONYMOUS))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:203.0.113.1"), eq(RateLimitType.ANONYMOUS))).thenReturn(45L);
        when(rateLimitService.getLimit(eq(RateLimitType.ANONYMOUS))).thenReturn(50);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("ip:203.0.113.1", RateLimitType.ANONYMOUS);
    }

    @Test
    @DisplayName("Should extract client IP from X-Real-IP header when X-Forwarded-For is not available")
    void shouldExtractClientIPFromXRealIPHeader() throws Exception {
        // Given
        when(rateLimitProperties.isEnabled()).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("198.51.100.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        when(rateLimitService.isAllowed(eq("ip:198.51.100.1"), eq(RateLimitType.ANONYMOUS))).thenReturn(true);
        when(rateLimitService.getAvailableTokens(eq("ip:198.51.100.1"), eq(RateLimitType.ANONYMOUS))).thenReturn(45L);
        when(rateLimitService.getLimit(eq(RateLimitType.ANONYMOUS))).thenReturn(50);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("ip:198.51.100.1", RateLimitType.ANONYMOUS);
    }
}
