package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for OAuth2 validation failures
 */
public class OAuth2ValidationException extends BaseException {

    public OAuth2ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "OAUTH2_VALIDATION_ERROR");
    }

    public OAuth2ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "OAUTH2_VALIDATION_ERROR");
    }
}
