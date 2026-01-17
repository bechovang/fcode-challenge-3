package com.gameaccountshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for creating a new game account listing
 * Contains Bean validation annotations with Vietnamese error messages
 */
public class ListingCreateRequest {

    @NotBlank(message = "Vui lòng nhập tên game")
    @Size(max = 100, message = "Tên game không được vượt quá 100 ký tự")
    private String gameName;

    @NotBlank(message = "Vui lòng nhập rank")
    @Size(max = 50, message = "Rank không được vượt quá 50 ký tự")
    private String rank;

    @NotNull(message = "Vui lòng nhập giá bán")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Vui lòng nhập mô tả")
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    // Default constructor
    public ListingCreateRequest() {
    }

    // Constructor for testing
    public ListingCreateRequest(String gameName, String rank, BigDecimal price, String description) {
        this.gameName = gameName;
        this.rank = rank;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
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
}
