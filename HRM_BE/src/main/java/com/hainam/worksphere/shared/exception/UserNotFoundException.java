package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static UserNotFoundException byId(String id) {
        return new UserNotFoundException("User not found with id: " + id);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }
}

