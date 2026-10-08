package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {

    private static final String ERROR_CODE = "INVALID_TOKEN";

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired");
    }

    public static InvalidTokenException malformed() {
        return new InvalidTokenException("Token is malformed");
    }

    public static InvalidTokenException invalid() {
        return new InvalidTokenException("Token is invalid");
    }
}
