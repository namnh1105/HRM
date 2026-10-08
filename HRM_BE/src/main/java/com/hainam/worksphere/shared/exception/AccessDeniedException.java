package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BaseException {

    private static final String ERROR_CODE = "ACCESS_DENIED";

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public static AccessDeniedException insufficientPermissions() {
        return new AccessDeniedException("Insufficient permissions to access this resource");
    }

    public static AccessDeniedException resourceNotOwned() {
        return new AccessDeniedException("You can only access your own resources");
    }
}
