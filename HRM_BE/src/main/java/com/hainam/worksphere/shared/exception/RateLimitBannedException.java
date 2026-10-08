package com.hainam.worksphere.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user/IP is temporarily banned due to excessive rate limit violations
 */
@Getter
public class RateLimitBannedException extends BaseException {

    private final long remainingBanTimeSeconds;
    private final String bannedKey;

    public RateLimitBannedException(String message, String bannedKey, long remainingBanTimeSeconds) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_BANNED");
        this.bannedKey = bannedKey;
        this.remainingBanTimeSeconds = remainingBanTimeSeconds;
    }

    public long getRetryAfterSeconds() {
        return remainingBanTimeSeconds;
    }
}
