package com.gameaccountshop.enums;

/**
 * Account status enumeration for game account listings
 *
 * Status Flow:
 * PENDING (new listing) → APPROVED (visible) → SOLD
 *                 → REJECTED (with reason)
 */
public enum AccountStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SOLD
}
