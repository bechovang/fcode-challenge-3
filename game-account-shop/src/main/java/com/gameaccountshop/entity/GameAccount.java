package com.gameaccountshop.entity;

import com.gameaccountshop.enums.ListingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_accounts")
public class GameAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Auto-set to "Liên Minh Huyền Thoại" for MVP (LoL-only)
    @Column(name = "game_name", length = 100)
    private String gameName = "Liên Minh Huyền Thoại";

    // Maps to database column "account_rank"
    @Column(name = "account_rank", nullable = false, length = 50)
    private String accountRank;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "account_username", length = 100)
    private String accountUsername;

    @Column(name = "account_password", length = 100)
    private String accountPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status = ListingStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ListingStatus.PENDING;
        }
        if (gameName == null) {
            gameName = "Liên Minh Huyền Thoại";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public String getAccountPassword() { return accountPassword; }
    public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
}
