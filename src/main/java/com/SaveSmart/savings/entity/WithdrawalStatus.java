package com.SaveSmart.savings.entity;

/**
 * Lifecycle of a secured withdrawal request.
 *
 * PENDING  - created by the user, no money has moved
 * APPROVED - a reviewer accepted it, money has been moved back to the bank
 * REJECTED - a reviewer refused it, money never moved
 */
public enum WithdrawalStatus {
    PENDING,
    APPROVED,
    REJECTED
}
