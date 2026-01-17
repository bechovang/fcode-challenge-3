package com.gameaccountshop.dto;

import com.gameaccountshop.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for GameAccount entity - used for API responses
 * Follows project pattern: DTOs for API responses, not entities
 */
public class GameAccountDTO {
    private Long id;
    private Long sellerId;
    private String gameName;
    private String rank;
    private BigDecimal price;
    private String description;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public GameAccountDTO() {
    }

    public GameAccountDTO(Long id, Long sellerId, String gameName, String rank,
                          BigDecimal price, String description, AccountStatus status,
                          LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.gameName = gameName;
        this.rank = rank;
        this.price = price;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
