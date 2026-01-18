package com.gameaccountshop.exception;

/**
 * Custom exception for business logic violations
 * Used for validating business rules like username uniqueness
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
