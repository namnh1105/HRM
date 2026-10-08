package com.hainam.worksphere.shared.ratelimit;

/**
 * Types of rate limits applied to different endpoints
 */
public enum RateLimitType {
    LOGIN,
    REGISTER,
    REFRESH_TOKEN,
    ANONYMOUS,
    AUTHENTICATED
}
