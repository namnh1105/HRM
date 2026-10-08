package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class WorkShiftNotFoundException extends BaseException {

    private static final String ERROR_CODE = "WORK_SHIFT_NOT_FOUND";

    public WorkShiftNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public WorkShiftNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static WorkShiftNotFoundException byId(String id) {
        return new WorkShiftNotFoundException("Work shift not found with id: " + id);
    }

    public static WorkShiftNotFoundException byCode(String code) {
        return new WorkShiftNotFoundException("Work shift not found with code: " + code);
    }
}
