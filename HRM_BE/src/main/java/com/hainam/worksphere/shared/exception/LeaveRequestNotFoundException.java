package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class LeaveRequestNotFoundException extends BaseException {

    private static final String ERROR_CODE = "LEAVE_REQUEST_NOT_FOUND";

    public LeaveRequestNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public LeaveRequestNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static LeaveRequestNotFoundException byId(String id) {
        return new LeaveRequestNotFoundException("Leave request not found with id: " + id);
    }
}
