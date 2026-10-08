package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier), 
              HttpStatus.NOT_FOUND, ERROR_CODE);
    }
}
