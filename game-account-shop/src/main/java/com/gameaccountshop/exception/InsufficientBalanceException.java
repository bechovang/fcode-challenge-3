package com.gameaccountshop.exception;

/**
 * Exception thrown when user has insufficient wallet balance
 * Story 3.1: Wallet System
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
