package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class InsuranceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "INSURANCE_NOT_FOUND";

    public InsuranceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public InsuranceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static InsuranceNotFoundException byId(String id) {
        return new InsuranceNotFoundException("Insurance not found with id: " + id);
    }
}
