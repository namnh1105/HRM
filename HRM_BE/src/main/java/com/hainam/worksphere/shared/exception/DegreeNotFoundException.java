package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class DegreeNotFoundException extends BaseException {

    private static final String ERROR_CODE = "DEGREE_NOT_FOUND";

    public DegreeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public DegreeNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static DegreeNotFoundException byId(String id) {
        return new DegreeNotFoundException("Degree not found with id: " + id);
    }
}
