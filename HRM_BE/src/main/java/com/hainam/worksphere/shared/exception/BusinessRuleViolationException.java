package com.hainam.worksphere.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for business rule validation failures
 */
public class BusinessRuleViolationException extends BaseException {

    public BusinessRuleViolationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }
}
