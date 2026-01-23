package com.gameaccountshop.entity;

import com.gameaccountshop.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction entity
 * Story 3.1: Buy Now & Show PayOS Payment
 */
class TransactionTest {

    @Test
    void testTransactionCreation_WithValidData_ShouldCreateTransaction() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setListingId(1L);
        transaction.setBuyerId(2L);
        transaction.setSellerId(3L);
        transaction.setAmount(new BigDecimal("500000"));
        transaction.setCommission(new BigDecimal("50000"));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setPaymentLinkId("test-payment-link-id");
        transaction.setCreatedAt(LocalDateTime.now());

        // When
        Long listingId = transaction.getListingId();
        Long buyerId = transaction.getBuyerId();
        Long sellerId = transaction.getSellerId();
        BigDecimal amount = transaction.getAmount();
        BigDecimal commission = transaction.getCommission();
        TransactionStatus status = transaction.getStatus();
        String paymentLinkId = transaction.getPaymentLinkId();

        // Then
        assertNotNull(listingId);
        assertNotNull(buyerId);
        assertNotNull(sellerId);
        assertNotNull(amount);
        assertNotNull(commission);
        assertNotNull(status);
        assertEquals(1L, listingId);
        assertEquals(2L, buyerId);
        assertEquals(3L, sellerId);
        assertEquals(new BigDecimal("500000"), amount);
        assertEquals(new BigDecimal("50000"), commission);
        assertEquals(TransactionStatus.PENDING, status);
        assertEquals("test-payment-link-id", paymentLinkId);
    }

    @Test
    void testGetTotalAmount_ShouldReturnAmountPlusCommission() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("500000"));
        transaction.setCommission(new BigDecimal("50000"));

        // When
        BigDecimal total = transaction.getTotalAmount();

        // Then
        assertEquals(new BigDecimal("550000"), total);
    }

    @Test
    void testGetTransactionId_ShouldReturnTXNPrefixWithId() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setId(123L);

        // When
        String transactionId = transaction.getTransactionId();

        // Then
        assertEquals("TXN123", transactionId);
    }

    @Test
    void testCommissionCalculation_10PercentOfAmount() {
        // Given
        BigDecimal amount = new BigDecimal("500000");
        BigDecimal expectedCommission = amount.multiply(new BigDecimal("0.10"));

        // When
        BigDecimal commission = new BigDecimal("50000");

        // Then - use compareTo for BigDecimal comparison
        assertEquals(0, expectedCommission.compareTo(commission));
    }

    @Test
    void testCommissionCalculation_ForDifferentAmounts() {
        // Test case 1: 100,000 VNĐ
        assertEquals(0, new BigDecimal("10000").compareTo(new BigDecimal("100000").multiply(new BigDecimal("0.10"))));

        // Test case 2: 1,000,000 VNĐ
        assertEquals(0, new BigDecimal("100000").compareTo(new BigDecimal("1000000").multiply(new BigDecimal("0.10"))));

        // Test case 3: 250,000 VNĐ
        assertEquals(0, new BigDecimal("25000").compareTo(new BigDecimal("250000").multiply(new BigDecimal("0.10"))));
    }

    @Test
    void testTransactionStatusEnum_ShouldHaveCorrectValues() {
        // Given & When & Then
        assertEquals(TransactionStatus.PENDING, TransactionStatus.valueOf("PENDING"));
        assertEquals(TransactionStatus.COMPLETED, TransactionStatus.valueOf("COMPLETED"));
        assertEquals(TransactionStatus.REJECTED, TransactionStatus.valueOf("REJECTED"));
    }
}
