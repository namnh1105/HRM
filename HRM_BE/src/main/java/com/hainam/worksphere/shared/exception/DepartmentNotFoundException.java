package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class DepartmentNotFoundException extends BaseException {

    private static final String ERROR_CODE = "DEPARTMENT_NOT_FOUND";

    public DepartmentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public DepartmentNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static DepartmentNotFoundException byId(String id) {
        return new DepartmentNotFoundException("Department not found with id: " + id);
    }

    public static DepartmentNotFoundException byCode(String code) {
        return new DepartmentNotFoundException("Department not found with code: " + code);
    }
}
