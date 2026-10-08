package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class StoreNotFoundException extends BaseException {

    private static final String ERROR_CODE = "STORE_NOT_FOUND";

    public StoreNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public StoreNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static StoreNotFoundException byId(String id) {
        return new StoreNotFoundException("Store not found with id: " + id);
    }

    public static StoreNotFoundException byCode(String code) {
        return new StoreNotFoundException("Store not found with code: " + code);
    }
}
