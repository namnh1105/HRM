package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class PayrollNotFoundException extends BaseException {

    private static final String ERROR_CODE = "PAYROLL_NOT_FOUND";

    public PayrollNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public PayrollNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static PayrollNotFoundException byId(String id) {
        return new PayrollNotFoundException("Payroll not found with id: " + id);
    }
}
