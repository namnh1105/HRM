package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenException extends BaseException {

    private static final String ERROR_CODE = "REFRESH_TOKEN_ERROR";

    public RefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public RefreshTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public static RefreshTokenException expired() {
        return new RefreshTokenException("Refresh token is expired. Please make a new signin request");
    }

    public static RefreshTokenException revoked() {
        return new RefreshTokenException("Refresh token is revoked. Please make a new signin request");
    }

    public static RefreshTokenException notFound() {
        return new RefreshTokenException("Refresh token is not in database");
    }
}
