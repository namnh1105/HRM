package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.shared.config.RateLimitProperties;
import com.hainam.worksphere.shared.exception.RateLimitBannedException;
import com.hainam.worksphere.shared.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public RateLimitFilter(RateLimitService rateLimitService,
                           RateLimitProperties rateLimitProperties,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.rateLimitService = rateLimitService;
        this.rateLimitProperties = rateLimitProperties;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitProperties.isEnabled()) {
            log.debug("Rate limiting is disabled, skipping filter");
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        RateLimitType rateLimitType = resolveRateLimitType(request);

        if (!rateLimitService.isAllowed(clientKey, rateLimitType)) {
            log.warn("Rate limit BLOCKED - clientKey: {}, type: {}", clientKey, rateLimitType);

            long remainingBanTime = rateLimitService.getRemainingBanTime(clientKey);

            if (remainingBanTime > 0) {
                String message = String.format(
                        "You are temporarily banned for %d more seconds due to excessive rate limit violations.",
                        remainingBanTime);
                handlerExceptionResolver.resolveException(request, response, null, 
                        new RateLimitBannedException(message, clientKey, remainingBanTime));
            } else {
                String message = "Rate limit exceeded. Please try again later.";
                handlerExceptionResolver.resolveException(request, response, null, 
                        new RateLimitExceededException(message, 60));
            }
            return;
        }

        addRateLimitHeaders(response, clientKey, rateLimitType);
        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        return "ip:" + getClientIP(request);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private RateLimitType resolveRateLimitType(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.contains("/auth/login"))
            return RateLimitType.LOGIN;
        if (path.contains("/auth/register"))
            return RateLimitType.REGISTER;
        if (path.contains("/auth/refresh"))
            return RateLimitType.REFRESH_TOKEN;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return RateLimitType.AUTHENTICATED;
        }

        return RateLimitType.ANONYMOUS;
    }

    private void addRateLimitHeaders(HttpServletResponse response, String clientKey, RateLimitType rateLimitType) {
        long availableTokens = rateLimitService.getAvailableTokens(clientKey, rateLimitType);
        int limit = rateLimitService.getLimit(rateLimitType);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", "60");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")
                || path.startsWith("/actuator/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
