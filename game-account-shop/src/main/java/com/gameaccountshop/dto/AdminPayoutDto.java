package com.gameaccountshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for admin payout page
 * Story 3.4: Admin Payout System
 */
public record AdminPayoutDto(
    Long id,
    Long sellerId,
    String sellerUsername,
    String sellerEmail,
    BigDecimal amount,
    String status,
    String statusDisplayName,
    LocalDateTime createdAt,
    LocalDateTime paidAt,
    LocalDateTime receivedAt,
    Long adminId
) {
    public static AdminPayoutDto fromEntity(com.gameaccountshop.entity.Payout payout, String sellerUsername, String sellerEmail) {
        return new AdminPayoutDto(
            payout.getId(),
            payout.getSellerId(),
            sellerUsername,
            sellerEmail,
            payout.getAmount(),
            payout.getStatus().name(),
            payout.getStatusDisplayName(),
            payout.getCreatedAt(),
            payout.getPaidAt(),
            payout.getReceivedAt(),
            payout.getAdminId()
        );
    }
}
