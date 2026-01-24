package com.gameaccountshop.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for seller pending payout summary
 * Story 3.4: Admin Payout System
 */
public record SellerPayoutSummaryDto(
    BigDecimal totalPendingAmount,
    int pendingCount,
    List<PendingPayoutItem> pendingPayouts
) {
    public record PendingPayoutItem(
        Long payoutId,
        BigDecimal amount
    ) {}
}
