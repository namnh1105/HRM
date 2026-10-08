package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    private static final String ERROR_CODE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public static InvalidCredentialsException create() {
        return new InvalidCredentialsException("Invalid email or password");
    }
}
