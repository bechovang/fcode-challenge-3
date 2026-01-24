package com.gameaccountshop.enums;

/**
 * Payout status enum
 * Story 3.4: Admin Payout System
 */
public enum PayoutStatus {
    /**
     * Payout created automatically on 1st of month
     * Waiting for admin to process
     */
    NEEDS_PAYMENT,

    /**
     * Admin has transferred money and clicked "Mark as Paid"
     * Waiting for seller to confirm receipt
     */
    PAID,

    /**
     * Seller has confirmed receipt
     * Payout is complete
     */
    RECEIVED
}
