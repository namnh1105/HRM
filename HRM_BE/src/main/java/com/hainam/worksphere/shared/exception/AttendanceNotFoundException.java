package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

public class AttendanceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "ATTENDANCE_NOT_FOUND";

    public AttendanceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public AttendanceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    public static AttendanceNotFoundException byId(String id) {
        return new AttendanceNotFoundException("Attendance not found with id: " + id);
    }
}
