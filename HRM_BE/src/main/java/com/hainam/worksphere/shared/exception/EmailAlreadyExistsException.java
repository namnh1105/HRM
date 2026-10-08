package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException {

    private static final String ERROR_CODE = "EMAIL_ALREADY_EXISTS";

    public EmailAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public static EmailAlreadyExistsException withEmail(String email) {
        return new EmailAlreadyExistsException("Email already exists: " + email);
    }
}
