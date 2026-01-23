package com.gameaccountshop.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin review page listings
 * Story 2.4: Admin Approve/Reject Listings
 * Includes seller username for display
 * Issue #13: Add imageUrl for viewing account description images
 */
public record AdminListingDto(
        Long id,
        String gameName,
        String accountRank,
        Long price,
        String description,
        String sellerUsername,
        LocalDateTime createdAt,
        String imageUrl
) {
}
