package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class RelativeNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RELATIVE_NOT_FOUND";

    public RelativeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public RelativeNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static RelativeNotFoundException byId(String id) {
        return new RelativeNotFoundException("Relative not found with id: " + id);
    }
}
