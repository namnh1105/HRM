package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public static ValidationException fieldError(String field, String error) {
        return new ValidationException("Validation failed for field '" + field + "': " + error);
    }

    public static ValidationException passwordMismatch() {
        return new ValidationException("Current password is incorrect");
    }

    public static ValidationException duplicateField(String field, String value) {
        return new ValidationException("Duplicate value '" + value + "' for field '" + field + "'");
    }
}
