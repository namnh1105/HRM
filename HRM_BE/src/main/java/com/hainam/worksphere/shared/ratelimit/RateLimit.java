package com.hainam.worksphere.shared.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply custom rate limiting to specific endpoints.
 * Can be applied at method or class level.
 *
 * Example usage:
 * <pre>
 * {@code
 * @RateLimit(requests = 10, duration = 60, type = RateLimitType.LOGIN)
 * public ResponseEntity<?> login(...) { ... }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed within the duration window.
     * Default is 100 requests.
     */
    int requests() default 100;

    /**
     * Duration window in seconds.
     * Default is 60 seconds (1 minute).
     */
    int duration() default 60;

    /**
     * Rate limit type to categorize the endpoint.
     * Default is AUTHENTICATED.
     */
    RateLimitType type() default RateLimitType.AUTHENTICATED;

    /**
     * Custom key prefix for rate limiting.
     * If empty, the default key resolution will be used.
     */
    String keyPrefix() default "";

    /**
     * Whether to include the user ID in the rate limit key.
     * Default is true for user-specific rate limiting.
     */
    boolean includeUserId() default true;

    /**
     * Whether to include the IP address in the rate limit key.
     * Default is true for IP-based rate limiting.
     */
    boolean includeIp() default true;
}

