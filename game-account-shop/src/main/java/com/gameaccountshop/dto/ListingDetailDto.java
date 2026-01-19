package com.gameaccountshop.dto;

import com.gameaccountshop.enums.ListingStatus;
import java.time.LocalDateTime;

/**
 * DTO for listing detail page (Story 2.3)
 * Includes all listing fields plus seller information
 *
 * Updated to use ListingStatus enum instead of String for type safety
 */
public class ListingDetailDto {
    // Listing fields
    private Long id;
    private String gameName;
    private String accountRank;
    private Long price;
    private String description;
    private ListingStatus status;  // Changed from String to ListingStatus enum
    private LocalDateTime createdAt;
    private LocalDateTime soldAt;

    // Seller fields
    private Long sellerId;
    private String sellerUsername;
    private String sellerEmail;

    // Default constructor
    public ListingDetailDto() {
    }

    // Full constructor for JPA @Query projection
    public ListingDetailDto(Long id, String gameName, String accountRank,
                             Long price, String description, ListingStatus status,
                             LocalDateTime createdAt, LocalDateTime soldAt,
                             Long sellerId, String sellerUsername, String sellerEmail) {
        this.id = id;
        this.gameName = gameName;
        this.accountRank = accountRank;
        this.price = price;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.soldAt = soldAt;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.sellerEmail = sellerEmail;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getAccountRank() {
        return accountRank;
    }

    public void setAccountRank(String accountRank) {
        this.accountRank = accountRank;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    // Convenience method to check if sold - now type-safe with enum
    public boolean isSold() {
        return ListingStatus.SOLD.equals(status);
    }

    // Convenience method to check if approved
    public boolean isApproved() {
        return ListingStatus.APPROVED.equals(status);
    }
}
