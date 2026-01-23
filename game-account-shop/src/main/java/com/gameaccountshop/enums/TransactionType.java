package com.gameaccountshop.enums;

/**
 * Transaction type enum
 * Story 3.1: Wallet System - Track different transaction types
 */
public enum TransactionType {
    /**
     * User tops up money into wallet (requires admin approval)
     */
    TOP_UP,

    /**
     * User purchases game account with wallet balance
     */
    PURCHASE,

    /**
     * User withdraws money from wallet (future feature)
     */
    WITHDRAWAL,

    /**
     * Refund to user wallet
     */
    REFUND
}
