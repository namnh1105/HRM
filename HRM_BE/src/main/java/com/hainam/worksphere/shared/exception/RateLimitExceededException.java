package com.hainam.worksphere.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded
 */
@Getter
public class RateLimitExceededException extends BaseException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = 60;
    }

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds > 0 ? retryAfterSeconds : 60;
    }
}

