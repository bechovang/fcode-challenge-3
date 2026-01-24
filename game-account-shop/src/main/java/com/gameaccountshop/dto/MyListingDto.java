package com.gameaccountshop.dto;

import java.time.LocalDateTime;

/**
 * DTO for seller's own listings (My Listings page)
 * Story 3.3: My Listings - Filtering & Profit Display
 */
public record MyListingDto(
        Long id,
        String gameName,
        String accountRank,
        Long price,
        String imageUrl,
        String status,
        LocalDateTime createdAt
) {
}
