package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for duplicate resource conflicts
 */
public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}
