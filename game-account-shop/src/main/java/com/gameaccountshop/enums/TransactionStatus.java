package com.gameaccountshop.enums;

/**
 * Transaction status enum
 * Story 3.1: Wallet System & Buy with Balance
 */
public enum TransactionStatus {
    /**
     * Transaction created, waiting for processing
     */
    PENDING,

    /**
     * Top-up approved or purchase completed
     */
    COMPLETED,

    /**
     * Transaction rejected (by admin for top-up, or system)
     */
    REJECTED
}
