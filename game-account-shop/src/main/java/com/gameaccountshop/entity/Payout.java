package com.gameaccountshop.entity;

import com.gameaccountshop.enums.PayoutStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payout entity
 * Story 3.4: Admin Payout System
 */
@Entity
@Table(name = "payouts",
    indexes = {
        @Index(name = "idx_seller_id", columnList = "seller_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_seller_status_month", columnList = "seller_id, status, created_at")
    })
public class Payout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayoutStatus status = PayoutStatus.NEEDS_PAYMENT;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "admin_id")
    private Long adminId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PayoutStatus.NEEDS_PAYMENT;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    /**
     * Get Vietnamese display name for status
     */
    public String getStatusDisplayName() {
        switch (status) {
            case NEEDS_PAYMENT: return "Chờ thanh toán";
            case PAID: return "Đã chuyển";
            case RECEIVED: return "Đã nhận";
            default: return "Không xác định";
        }
    }

    /**
     * Get CSS class for status badge
     */
    public String getStatusClass() {
        switch (status) {
            case NEEDS_PAYMENT: return "bg-warning text-dark";
            case PAID: return "bg-info text-dark";
            case RECEIVED: return "bg-success";
            default: return "bg-secondary";
        }
    }
}
