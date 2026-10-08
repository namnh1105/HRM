package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class ContractNotFoundException extends BaseException {

    private static final String ERROR_CODE = "CONTRACT_NOT_FOUND";

    public ContractNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public ContractNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static ContractNotFoundException byId(String id) {
        return new ContractNotFoundException("Contract not found with id: " + id);
    }
}
