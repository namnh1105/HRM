package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class EmployeeNotFoundException extends BaseException {

    private static final String ERROR_CODE = "EMPLOYEE_NOT_FOUND";

    public EmployeeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static EmployeeNotFoundException byId(String id) {
        return new EmployeeNotFoundException("Employee not found with id: " + id);
    }

    public static EmployeeNotFoundException byUserId(String userId) {
        return new EmployeeNotFoundException("Employee not found with user id: " + userId);
    }
}
