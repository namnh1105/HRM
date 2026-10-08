package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class UserInactiveException extends BaseException {

    private static final String ERROR_CODE = "USER_INACTIVE";

    public UserInactiveException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public UserInactiveException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public static UserInactiveException create(String email) {
        return new UserInactiveException("User account is inactive: " + email);
    }
}
