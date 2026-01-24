package com.gameaccountshop.service;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.entity.Wallet;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import com.gameaccountshop.exception.InsufficientBalanceException;
import com.gameaccountshop.exception.ResourceNotFoundException;
import com.gameaccountshop.repository.TransactionRepository;
import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Wallet service
 * Story 3.1: Wallet System - Manage user wallet balance
 * Story 3.2: Top-up Approval Email Notifications - Email notifications for top-up approval/rejection
 */
@Service
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;  // Story 3.2
    private final EmailService emailService;      // Story 3.2

    public WalletService(WalletRepository walletRepository,
                          TransactionRepository transactionRepository,
                          UserRepository userRepository,      // Story 3.2
                          EmailService emailService) {       // Story 3.2
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;                 // Story 3.2
        this.emailService = emailService;                     // Story 3.2
    }

    /**
     * Get or create wallet for user
     * @param userId User ID
     * @return User's wallet
     */
    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new wallet for user: {}", userId);
                    Wallet wallet = new Wallet();
                    wallet.setUserId(userId);
                    wallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Get wallet balance for user
     * @param userId User ID
     * @return Current balance
     */
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return wallet.getBalance();
    }

    /**
     * Check if user has sufficient balance
     * @param userId User ID
     * @param amount Required amount
     * @return true if sufficient, false otherwise
     */
    public boolean hasBalance(Long userId, BigDecimal amount) {
        BigDecimal balance = getBalance(userId);
        return balance.compareTo(amount) >= 0;
    }

    /**
     * Deduct balance from wallet (for purchases)
     * @param userId User ID
     * @param amount Amount to deduct
     * @throws InsufficientBalanceException if insufficient balance
     */
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for user {}: has={}, needs={}", userId, wallet.getBalance(), amount);
            throw new InsufficientBalanceException("Số dư không đủ. Vui lòng nạp thêm tiền vào ví.");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Deducted {} from wallet for user {}, new balance: {}", amount, userId, newBalance);
    }

    /**
     * Add balance to wallet (for approved top-ups)
     * @param userId User ID
     * @param amount Amount to add
     */
    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Added {} to wallet for user {}, new balance: {}", amount, userId, newBalance);
    }

    /**
     * Create a top-up transaction with PayOS QR code
     * @param userId User ID requesting top-up
     * @param amount Amount to top-up
     * @param transactionService TransactionService for PayOS integration
     * @return Created transaction with PayOS data
     */
    @Transactional
    public Transaction createTopUpTransaction(Long userId, BigDecimal amount, TransactionService transactionService) {
        log.info("Creating top-up transaction for user: {}, amount: {}", userId, amount);

        // Validate amount (minimum 10k, maximum 10M)
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 10,000 VNĐ");
        }
        if (amount.compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("Số tiền nạp tối đa là 10,000,000 VNĐ");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setBuyerId(userId); // Use buyerId as the user requesting top-up
        transaction.setSellerId(userId); // Use sellerId as same user for top-up
        transaction.setListingId(null); // No listing for top-up - NULL, not 0L
        transaction.setAmount(amount);
        transaction.setCommission(BigDecimal.ZERO); // No commission for top-up
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionType(TransactionType.TOP_UP);

        // Save transaction first to get the ID
        Transaction saved = transactionRepository.save(transaction);

        // Create PayOS payment for top-up
        // Description format: "Nap vi {amount}" - must be ASCII, <= 25 chars
        String description = "Nap vi " + amount.intValue();
        var payOSData = transactionService.getPayOSService().createTopUpPayment(
                amount,
                "TOPUP" + saved.getId(),
                description
        );

        // Update transaction with PayOS data
        saved.setPaymentLinkId(payOSData.getPaymentLinkId());
        saved.setQrCode(payOSData.getQrCode());
        saved.setCheckoutUrl(payOSData.getCheckoutUrl());
        saved = transactionRepository.save(saved);

        log.info("Top-up transaction created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Approve a top-up transaction and add balance to user's wallet
     * Story 3.2: Send email notification after approval
     * @param transactionId Transaction ID to approve
     * @param adminId Admin user ID who is approving
     */
    @Transactional
    public void approveTopUp(Long transactionId, Long adminId) {
        log.info("Admin {} approving top-up transaction: {}", adminId, transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch này"));

        if (transaction.getTransactionType() != TransactionType.TOP_UP) {
            throw new IllegalArgumentException("Giao dịch này không phải là giao dịch nạp tiền");
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Giao dịch này đã được xử lý");
        }

        // Add balance to user's wallet
        addBalance(transaction.getBuyerId(), transaction.getAmount());

        // Get new balance for email
        BigDecimal newBalance = getBalance(transaction.getBuyerId());

        // Update transaction status
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setApprovedBy(adminId);
        transaction.setApprovedAt(java.time.LocalDateTime.now());
        transactionRepository.save(transaction);

        // Story 3.2: Send email notification
        try {
            User user = userRepository.findById(transaction.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            String transactionIdStr = "TXN" + transaction.getId();
            emailService.sendTopUpApprovedEmail(
                    user.getEmail(),
                    transaction.getAmount(),
                    newBalance,
                    transactionIdStr
            );
            log.info("Top-up approval email sent to user: {}", transaction.getBuyerId());
        } catch (Exception e) {
            log.error("Failed to send top-up approval email for transaction: {}", transactionId, e);
            // Don't throw - transaction is already approved
        }

        log.info("Top-up transaction {} approved, balance added for user: {}", transactionId, transaction.getBuyerId());
    }

    /**
     * Reject a top-up transaction
     * Story 3.2: Send email notification after rejection
     * @param transactionId Transaction ID to reject
     * @param adminId Admin user ID who is rejecting
     * @param reason Rejection reason
     */
    @Transactional
    public void rejectTopUp(Long transactionId, Long adminId, String reason) {
        log.info("Admin {} rejecting top-up transaction: {}, reason: {}", adminId, transactionId, reason);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch này"));

        if (transaction.getTransactionType() != TransactionType.TOP_UP) {
            throw new IllegalArgumentException("Giao dịch này không phải là giao dịch nạp tiền");
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Giao dịch này đã được xử lý");
        }

        // Update transaction status
        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setApprovedBy(adminId);
        transaction.setApprovedAt(java.time.LocalDateTime.now());
        transaction.setRejectionReason(reason);
        transactionRepository.save(transaction);

        // Story 3.2: Send email notification
        try {
            User user = userRepository.findById(transaction.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            String transactionIdStr = "TXN" + transaction.getId();
            emailService.sendTopUpRejectedEmail(
                    user.getEmail(),
                    transaction.getAmount(),
                    reason,
                    transactionIdStr
            );
            log.info("Top-up rejection email sent to user: {}", transaction.getBuyerId());
        } catch (Exception e) {
            log.error("Failed to send top-up rejection email for transaction: {}", transactionId, e);
            // Don't throw - transaction is already rejected
        }

        log.info("Top-up transaction {} rejected", transactionId);
    }

    /**
     * Get all pending top-up transactions
     * @return List of pending top-up transactions
     */
    public java.util.List<Transaction> getPendingTopUps() {
        return transactionRepository.findByStatusAndTransactionTypeOrderByCreatedAtDesc(
                TransactionStatus.PENDING,
                TransactionType.TOP_UP
        );
    }
}
