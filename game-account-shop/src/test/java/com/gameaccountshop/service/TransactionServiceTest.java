package com.gameaccountshop.service;

import com.gameaccountshop.dto.PayOSResponse;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.exception.ResourceNotFoundException;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TransactionService
 * Story 3.1: Buy Now & Show PayOS Payment
 * Review follow-up: Add comprehensive integration tests
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private GameAccountRepository gameAccountRepository;

    @Mock
    private PayOSService payOSService;

    @InjectMocks
    private TransactionService transactionService;

    private GameAccount approvedListing;
    private GameAccount pendingListing;
    private Transaction testTransaction;
    private PayOSResponse.PayOSData mockPayOSData;

    @BeforeEach
    void setUp() {
        // Approved listing (can be purchased)
        approvedListing = new GameAccount();
        approvedListing.setId(1L);
        approvedListing.setGameName("Liên Minh Huyền Thoại");
        approvedListing.setAccountRank("Gold III");
        approvedListing.setPrice(500000L);
        approvedListing.setDescription("Tài khoản Gold 30 tướng");
        approvedListing.setSellerId(2L);
        approvedListing.setStatus(ListingStatus.APPROVED);
        approvedListing.setCreatedAt(LocalDateTime.now());

        // Pending listing (cannot be purchased)
        pendingListing = new GameAccount();
        pendingListing.setId(2L);
        pendingListing.setGameName("Liên Minh Huyền Thoại");
        pendingListing.setAccountRank("Diamond II");
        pendingListing.setPrice(1000000L);
        pendingListing.setDescription("Tài khoản Diamond");
        pendingListing.setSellerId(2L);
        pendingListing.setStatus(ListingStatus.PENDING);
        pendingListing.setCreatedAt(LocalDateTime.now());

        // Test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setListingId(1L);
        testTransaction.setBuyerId(3L);
        testTransaction.setSellerId(2L);
        testTransaction.setAmount(new BigDecimal("500000"));
        testTransaction.setCommission(new BigDecimal("50000"));
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setPaymentLinkId("test-payment-link-id");
        testTransaction.setQrCode("000201010212...");
        testTransaction.setCheckoutUrl("https://checkout.payos.vn/web/test");
        testTransaction.setCreatedAt(LocalDateTime.now());

        // Mock PayOS data
        mockPayOSData = new PayOSResponse.PayOSData();
        mockPayOSData.setQrCode("000201010212...");
        mockPayOSData.setCheckoutUrl("https://checkout.payos.vn/web/test");
        mockPayOSData.setPaymentLinkId("test-payment-link-id");
        mockPayOSData.setOrderCode(1234567890);
        mockPayOSData.setAmount(550000);
        mockPayOSData.setStatus("PENDING");
    }

    // ========================================================================
    // Story 3.1: createTransaction Tests
    // ========================================================================

    @Test
    void createTransaction_ValidApprovedListing_CreatesTransactionWithCorrectData() {
        // Given
        Long listingId = 1L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(1L);
            return txn;
        });

        // When
        Transaction result = transactionService.createTransaction(listingId, buyerId);

        // Then
        assertNotNull(result);
        assertEquals(listingId, result.getListingId());
        assertEquals(buyerId, result.getBuyerId());
        assertEquals(2L, result.getSellerId()); // From approvedListing.getSellerId()
        assertEquals(0, new BigDecimal("500000").compareTo(result.getAmount()));
        assertEquals(0, new BigDecimal("50000").compareTo(result.getCommission())); // 10% of 500000
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals("000201010212...", result.getQrCode());
        assertEquals("https://checkout.payos.vn/web/test", result.getCheckoutUrl());
        assertEquals("test-payment-link-id", result.getPaymentLinkId());

        // Total amount should be 550000 (500000 + 50000)
        assertEquals(0, new BigDecimal("550000").compareTo(result.getTotalAmount()));

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, times(1)).createPayment(any(BigDecimal.class), eq("TXN" + listingId), eq("Thanh toan don hang " + listingId));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_CalculatesCommissionCorrectly() {
        // Given - Test different price points
        Long listingId = 1L;
        Long buyerId = 3L;
        approvedListing.setPrice(1000000L); // 1 million VND

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.createTransaction(listingId, buyerId);

        // Then - Commission should be 10% of listing price
        assertEquals(0, new BigDecimal("1000000").compareTo(result.getAmount()));
        assertEquals(0, new BigDecimal("100000").compareTo(result.getCommission())); // 10% of 1,000,000 = 100,000
        assertEquals(0, new BigDecimal("1100000").compareTo(result.getTotalAmount())); // Total = 1,000,000 + 100,000 = 1,100,000

        // Verify PayOS was called with correct parameters
        verify(payOSService).createPayment(any(BigDecimal.class), eq("TXN" + listingId), eq("Thanh toan don hang " + listingId));
    }

    @Test
    void createTransaction_BuyerIsSeller_ThrowsIllegalStateException() {
        // Given - Buyer attempting to purchase their own listing
        Long listingId = 1L;
        Long buyerId = 2L; // Same as approvedListing.getSellerId()

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.createTransaction(listingId, buyerId)
        );

        assertEquals("Bạn không thể mua tài khoản của chính mình", exception.getMessage());

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, never()).createPayment(any(), anyString(), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ListingNotApproved_ThrowsIllegalStateException() {
        // Given - PENDING listing cannot be purchased
        Long listingId = 2L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(pendingListing));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.createTransaction(listingId, buyerId)
        );

        assertEquals("Tài khoản này hiện không có sẵn để mua", exception.getMessage());

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, never()).createPayment(any(), anyString(), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ListingNotFound_ThrowsResourceNotFoundException() {
        // Given - Non-existent listing
        Long listingId = 999L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.createTransaction(listingId, buyerId)
        );

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, never()).createPayment(any(), anyString(), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_SoldListing_ThrowsIllegalStateException() {
        // Given - SOLD listing cannot be purchased
        GameAccount soldListing = new GameAccount();
        soldListing.setId(3L);
        soldListing.setGameName("Liên Minh Huyền Thoại");
        soldListing.setPrice(500000L);
        soldListing.setSellerId(2L);
        soldListing.setStatus(ListingStatus.SOLD);
        soldListing.setSoldAt(LocalDateTime.now());

        Long listingId = 3L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(soldListing));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.createTransaction(listingId, buyerId)
        );

        assertEquals("Tài khoản này hiện không có sẵn để mua", exception.getMessage());

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, never()).createPayment(any(), anyString(), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ========================================================================
    // Story 3.1: getTransaction Tests
    // ========================================================================

    @Test
    void getTransaction_ExistingTransaction_ReturnsTransaction() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        Transaction result = transactionService.getTransaction(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(TransactionStatus.PENDING, result.getStatus());

        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void getTransaction_NonExistentTransaction_ThrowsResourceNotFoundException() {
        // Given
        Long transactionId = 999L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransaction(transactionId)
        );

        assertEquals("Không tìm thấy giao dịch này", exception.getMessage());

        verify(transactionRepository, times(1)).findById(transactionId);
    }

    // ========================================================================
    // Story 3.1: getPendingTransactions Tests
    // ========================================================================

    @Test
    void getPendingTransactions_ExistingPendingTransactions_ReturnsListOrderedByCreatedAtDesc() {
        // Given
        Transaction txn1 = new Transaction();
        txn1.setId(1L);
        txn1.setStatus(TransactionStatus.PENDING);
        txn1.setCreatedAt(LocalDateTime.of(2026, 1, 20, 10, 0));

        Transaction txn2 = new Transaction();
        txn2.setId(2L);
        txn2.setStatus(TransactionStatus.PENDING);
        txn2.setCreatedAt(LocalDateTime.of(2026, 1, 18, 10, 0));

        Transaction txn3 = new Transaction();
        txn3.setId(3L);
        txn3.setStatus(TransactionStatus.PENDING);
        txn3.setCreatedAt(LocalDateTime.of(2026, 1, 19, 10, 0));

        // Repository returns data ordered by created_at DESC (newest first)
        List<Transaction> pendingTransactions = Arrays.asList(txn1, txn3, txn2);

        when(transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING))
            .thenReturn(pendingTransactions);

        // When
        List<Transaction> result = transactionService.getPendingTransactions();

        // Then - Should be ordered by created_at DESC (newest first)
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId()); // 2026-01-20 (newest)
        assertEquals(3L, result.get(1).getId()); // 2026-01-19
        assertEquals(2L, result.get(2).getId()); // 2026-01-18 (oldest)

        verify(transactionRepository, times(1)).findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING);
    }

    @Test
    void getPendingTransactions_NoPendingTransactions_ReturnsEmptyList() {
        // Given
        when(transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING))
            .thenReturn(Collections.emptyList());

        // When
        List<Transaction> result = transactionService.getPendingTransactions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository, times(1)).findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING);
    }

    // ========================================================================
    // Story 3.1: getTransactionsByBuyer Tests
    // ========================================================================

    @Test
    void getTransactionsByBuyer_ExistingTransactions_ReturnsListOfBuyerTransactions() {
        // Given
        Long buyerId = 3L;

        Transaction txn1 = new Transaction();
        txn1.setId(1L);
        txn1.setBuyerId(buyerId);
        txn1.setStatus(TransactionStatus.PENDING);

        Transaction txn2 = new Transaction();
        txn2.setId(2L);
        txn2.setBuyerId(buyerId);
        txn2.setStatus(TransactionStatus.VERIFIED);

        List<Transaction> buyerTransactions = Arrays.asList(txn1, txn2);

        when(transactionRepository.findByBuyerId(buyerId)).thenReturn(buyerTransactions);

        // When
        List<Transaction> result = transactionService.getTransactionsByBuyer(buyerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getBuyerId().equals(buyerId)));

        verify(transactionRepository, times(1)).findByBuyerId(buyerId);
    }

    @Test
    void getTransactionsByBuyer_NoTransactions_ReturnsEmptyList() {
        // Given
        Long buyerId = 999L;
        when(transactionRepository.findByBuyerId(buyerId)).thenReturn(Collections.emptyList());

        // When
        List<Transaction> result = transactionService.getTransactionsByBuyer(buyerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository, times(1)).findByBuyerId(buyerId);
    }

    // ========================================================================
    // Story 3.1: getTransactionsBySeller Tests
    // ========================================================================

    @Test
    void getTransactionsBySeller_ExistingTransactions_ReturnsListOfSellerTransactions() {
        // Given
        Long sellerId = 2L;

        Transaction txn1 = new Transaction();
        txn1.setId(1L);
        txn1.setSellerId(sellerId);
        txn1.setStatus(TransactionStatus.PENDING);

        Transaction txn2 = new Transaction();
        txn2.setId(3L);
        txn2.setSellerId(sellerId);
        txn2.setStatus(TransactionStatus.VERIFIED);

        List<Transaction> sellerTransactions = Arrays.asList(txn1, txn2);

        when(transactionRepository.findBySellerId(sellerId)).thenReturn(sellerTransactions);

        // When
        List<Transaction> result = transactionService.getTransactionsBySeller(sellerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getSellerId().equals(sellerId)));

        verify(transactionRepository, times(1)).findBySellerId(sellerId);
    }

    @Test
    void getTransactionsBySeller_NoTransactions_ReturnsEmptyList() {
        // Given
        Long sellerId = 999L;
        when(transactionRepository.findBySellerId(sellerId)).thenReturn(Collections.emptyList());

        // When
        List<Transaction> result = transactionService.getTransactionsBySeller(sellerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository, times(1)).findBySellerId(sellerId);
    }

    // ========================================================================
    // Story 3.1: PayOS Integration Tests
    // ========================================================================

    @Test
    void createTransaction_CallsPayOSServiceWithCorrectParameters() {
        // Given
        Long listingId = 1L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transactionService.createTransaction(listingId, buyerId);

        // Then - Verify PayOS was called with:
        // - Total amount (listing price + 10% commission)
        // - Transaction ID (TXN + listing ID)
        // - Description
        verify(payOSService, times(1)).createPayment(
            any(BigDecimal.class), // 500000 + 50000 = 550000
            eq("TXN" + listingId),
            eq("Thanh toan don hang " + listingId)
        );
    }

    @Test
    void createTransaction_StoresPayOSResponseDataInTransaction() {
        // Given
        Long listingId = 1L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.createTransaction(listingId, buyerId);

        // Then - Verify PayOS response data is stored
        assertEquals(mockPayOSData.getPaymentLinkId(), result.getPaymentLinkId());
        assertEquals(mockPayOSData.getQrCode(), result.getQrCode());
        assertEquals(mockPayOSData.getCheckoutUrl(), result.getCheckoutUrl());
    }

    @Test
    void createTransaction_PayOSServiceThrowsRuntimeException_PropagatesException() {
        // Given
        Long listingId = 1L;
        Long buyerId = 3L;

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString()))
            .thenThrow(new RuntimeException("Dịch vụ thanh toán hiện không khả dụng"));

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionService.createTransaction(listingId, buyerId)
        );

        assertTrue(exception.getMessage().contains("Dịch vụ thanh toán hiện không khả dụng"));

        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(payOSService, times(1)).createPayment(any(BigDecimal.class), anyString(), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ========================================================================
    // Story 3.1: Edge Cases and Validation Tests
    // ========================================================================

    @Test
    void createTransaction_ZeroPriceListing_CalculatesZeroCommission() {
        // Given - Edge case: zero price (should not happen in production but test for robustness)
        Long listingId = 1L;
        Long buyerId = 3L;
        approvedListing.setPrice(0L);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.createTransaction(listingId, buyerId);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getCommission()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalAmount()));
    }

    @Test
    void createTransaction_VeryLargePrice_CalculatesCommissionCorrectly() {
        // Given - Edge case: very large price
        Long listingId = 1L;
        Long buyerId = 3L;
        approvedListing.setPrice(100000000L); // 100 million VND

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(payOSService.createPayment(any(BigDecimal.class), anyString(), anyString())).thenReturn(mockPayOSData);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.createTransaction(listingId, buyerId);

        // Then
        assertEquals(0, new BigDecimal("100000000").compareTo(result.getAmount()));
        assertEquals(0, new BigDecimal("10000000").compareTo(result.getCommission())); // 10% of 100M = 10M
        assertEquals(0, new BigDecimal("110000000").compareTo(result.getTotalAmount()));
    }

    @Test
    void getTransaction_VerifiedTransaction_ReturnsVerifiedTransaction() {
        // Given
        Long transactionId = 1L;
        testTransaction.setStatus(TransactionStatus.VERIFIED);
        testTransaction.setVerifiedAt(LocalDateTime.now());

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        Transaction result = transactionService.getTransaction(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.VERIFIED, result.getStatus());
        assertNotNull(result.getVerifiedAt());
    }

    @Test
    void getTransaction_RejectedTransaction_ReturnsRejectedTransactionWithReason() {
        // Given
        Long transactionId = 1L;
        testTransaction.setStatus(TransactionStatus.REJECTED);
        testTransaction.setRejectionReason("Thanh toán không thành công");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        Transaction result = transactionService.getTransaction(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("Thanh toán không thành công", result.getRejectionReason());
    }
}
