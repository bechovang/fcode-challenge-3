package com.gameaccountshop.dto;

import java.time.LocalDateTime;

/**
 * DTO for displaying listings on browse page (Story 2.2)
 * Includes seller username from join with users table
 */
public class ListingDisplayDto {
    private Long id;
    private String gameName;
    private String accountRank;
    private Long price;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String sellerUsername;

    // Default constructor
    public ListingDisplayDto() {
    }

    // Full constructor for JPA @Query projection
    public ListingDisplayDto(Long id, String gameName, String accountRank,
                             Long price, String description, String imageUrl, LocalDateTime createdAt,
                             String sellerUsername) {
        this.id = id;
        this.gameName = gameName;
        this.accountRank = accountRank;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.sellerUsername = sellerUsername;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }
}
