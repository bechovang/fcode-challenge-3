package com.gameaccountshop.entity;

import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity
 * Story 3.1: Wallet System & Buy with Balance
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = true)
    private Long listingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "commission", nullable = false, precision = 12, scale = 2)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "qr_code", length = 1000)
    private String qrCode;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    /**
     * Calculate total amount (listing price + commission)
     */
    public BigDecimal getTotalAmount() {
        if (amount == null) {
            return commission;
        }
        if (commission == null) {
            return amount;
        }
        return amount.add(commission);
    }

    /**
     * Get transaction ID with TXN prefix
     */
    public String getTransactionId() {
        return "TXN" + id;
    }

    /**
     * Get CSS class for transaction type badge
     * Story 3.1: Wallet System - Used in wallet.html
     */
    public String getTransactionTypeClass() {
        if (transactionType == null) {
            return "type-refund";
        }
        switch (transactionType) {
            case TOP_UP:
                return "type-topup";
            case PURCHASE:
                return "type-purchase";
            case WITHDRAWAL:
                return "type-withdrawal";
            case REFUND:
                return "type-refund";
            default:
                return "type-refund";
        }
    }

    /**
     * Get CSS class for transaction status badge
     * Story 3.1: Wallet System - Used in wallet.html
     */
    public String getStatusClass() {
        if (status == null) {
            return "status-pending";
        }
        switch (status) {
            case PENDING:
                return "status-pending";
            case COMPLETED:
                return "status-completed";
            case REJECTED:
                return "status-rejected";
            default:
                return "status-pending";
        }
    }

    /**
     * Get display name for transaction type (in Vietnamese)
     * Story 3.1: Wallet System - Used in wallet.html
     */
    public String getTransactionTypeDisplayName() {
        if (transactionType == null) {
            return "Không xác định";
        }
        switch (transactionType) {
            case TOP_UP:
                return "Nạp tiền";
            case PURCHASE:
                return "Mua tài khoản";
            case WITHDRAWAL:
                return "Rút tiền";
            case REFUND:
                return "Hoàn tiền";
            default:
                return "Không xác định";
        }
    }

    /**
     * Get display name for transaction status (in Vietnamese)
     * Story 3.1: Wallet System - Used in wallet.html
     */
    public String getStatusDisplayName() {
        if (status == null) {
            return "⏳ Chờ duyệt";
        }
        switch (status) {
            case PENDING:
                return "⏳ Chờ duyệt";
            case COMPLETED:
                return "✓ Hoàn thành";
            case REJECTED:
                return "✗ Từ chối";
            default:
                return "⏳ Chờ duyệt";
        }
    }

    /**
     * Check if this transaction shows positive amount (add to wallet)
     * Used for displaying + or - prefix in wallet.html
     */
    public boolean isPositiveAmount() {
        if (transactionType == null) {
            return false;
        }
        return transactionType == TransactionType.TOP_UP || transactionType == TransactionType.REFUND;
    }
}
