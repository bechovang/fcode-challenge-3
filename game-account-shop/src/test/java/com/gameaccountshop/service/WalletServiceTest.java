package com.gameaccountshop.service;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.entity.Wallet;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import com.gameaccountshop.repository.TransactionRepository;
import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private WalletService walletService;

    private Wallet testWallet;
    private Transaction testTransaction;
    private User testUser;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUserId(100L);
        testWallet.setBalance(BigDecimal.ZERO);

        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setBuyerId(100L);
        testTransaction.setSellerId(100L); // same as buyer for top-up
        testTransaction.setListingId(null);
        testTransaction.setAmount(new BigDecimal("500000"));
        testTransaction.setCommission(BigDecimal.ZERO);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setTransactionType(TransactionType.TOP_UP);
    }

    @Test
    void approveTopUp_WhenValidTransaction_ShouldAddBalanceAndUpdateStatus() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendTopUpApprovedEmail(any(), any(), any(), any());

        // Act
        walletService.approveTopUp(1L, 1L);

        // Assert
        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        assertEquals(1L, testTransaction.getApprovedBy());
        assertNotNull(testTransaction.getApprovedAt());
        assertEquals(new BigDecimal("500000"), testWallet.getBalance());

        verify(emailService, times(1)).sendTopUpApprovedEmail(
                eq("user@example.com"),
                eq(new BigDecimal("500000")),
                eq(new BigDecimal("500000")),
                eq("TXN1")
        );
    }

    @Test
    void approveTopUp_WhenEmailServiceThrowsException_ShouldStillCompleteApproval() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("Email error")).when(emailService).sendTopUpApprovedEmail(any(), any(), any(), any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> walletService.approveTopUp(1L, 1L));

        // Assert - transaction should still be approved
        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        assertEquals(new BigDecimal("500000"), testWallet.getBalance());
    }

    @Test
    void rejectTopUp_WhenValidTransaction_ShouldUpdateStatusWithReason() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendTopUpRejectedEmail(any(), any(), any(), any());

        // Act
        walletService.rejectTopUp(1L, 1L, "Giao dịch không hợp lệ");

        // Assert
        assertEquals(TransactionStatus.REJECTED, testTransaction.getStatus());
        assertEquals(1L, testTransaction.getApprovedBy());
        assertEquals("Giao dịch không hợp lệ", testTransaction.getRejectionReason());
        assertNotNull(testTransaction.getApprovedAt());

        verify(emailService, times(1)).sendTopUpRejectedEmail(
                eq("user@example.com"),
                eq(new BigDecimal("500000")),
                eq("Giao dịch không hợp lệ"),
                eq("TXN1")
        );
    }

    @Test
    void rejectTopUp_WhenEmailServiceThrowsException_ShouldStillCompleteRejection() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("Email error")).when(emailService).sendTopUpRejectedEmail(any(), any(), any(), any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> walletService.rejectTopUp(1L, 1L, "Giao dịch không hợp lệ"));

        // Assert - transaction should still be rejected
        assertEquals(TransactionStatus.REJECTED, testTransaction.getStatus());
        assertEquals("Giao dịch không hợp lệ", testTransaction.getRejectionReason());
    }

    @Test
    void approveTopUp_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.approveTopUp(999L, 1L));
    }

    @Test
    void approveTopUp_WhenTransactionNotTopUpType_ShouldThrowException() {
        // Arrange
        testTransaction.setTransactionType(TransactionType.PURCHASE);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.approveTopUp(1L, 1L));
    }

    @Test
    void approveTopUp_WhenTransactionNotPending_ShouldThrowException() {
        // Arrange
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.approveTopUp(1L, 1L));
    }
}
