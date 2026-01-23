package com.gameaccountshop.service;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.exception.ResourceNotFoundException;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Transaction service
 * Story 3.1: Buy Now & Show PayOS Payment
 */
@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GameAccountRepository gameAccountRepository;
    private final PayOSService payOSService;

    public TransactionService(TransactionRepository transactionRepository,
                              GameAccountRepository gameAccountRepository,
                              PayOSService payOSService) {
        this.transactionRepository = transactionRepository;
        this.gameAccountRepository = gameAccountRepository;
        this.payOSService = payOSService;
    }

    /**
     * Create a new transaction for buying a listing
     * @param listingId ID of the listing being purchased
     * @param buyerId ID of the buyer
     * @return Created transaction
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException if listing is not available for purchase
     */
    @Transactional
    public Transaction createTransaction(Long listingId, Long buyerId) {
        log.info("Creating transaction for listing: {} by buyer: {}", listingId, buyerId);

        // Get listing
        GameAccount listing = gameAccountRepository.findById(listingId)
            .orElseThrow(() -> {
                log.warn("Listing not found: {}", listingId);
                return new ResourceNotFoundException("Không tìm thấy tài khoản này");
            });

        // Validate listing status
        if (listing.getStatus() != ListingStatus.APPROVED) {
            log.warn("Listing is not available for purchase: status={}", listing.getStatus());
            throw new IllegalStateException("Tài khoản này hiện không có sẵn để mua");
        }

        // Prevent buying own listing
        if (listing.getSellerId().equals(buyerId)) {
            log.warn("Seller attempting to buy own listing: sellerId={}", listing.getSellerId());
            throw new IllegalStateException("Bạn không thể mua tài khoản của chính mình");
        }

        // Convert price from Long (VNĐ) to BigDecimal for calculation
        BigDecimal listingPrice = new BigDecimal(listing.getPrice());

        // Calculate commission (10%)
        BigDecimal commission = listingPrice.multiply(new BigDecimal("0.10"));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setListingId(listingId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(listing.getSellerId());
        transaction.setAmount(listingPrice);
        transaction.setCommission(commission);
        transaction.setStatus(TransactionStatus.PENDING);

        // Save transaction first to get the transaction ID
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", saved.getId());

        // Create PayOS payment and store payment link ID, QR code, and checkout URL
        // Description must be <= 25 characters and ASCII-only for PayOS
        String description = "DH" + saved.getId();
        var payOSData = payOSService.createPayment(
            saved.getTotalAmount(),
            "TXN" + saved.getId(),
            description
        );

        // Update transaction with PayOS data
        saved.setPaymentLinkId(payOSData.getPaymentLinkId());
        saved.setQrCode(payOSData.getQrCode());
        saved.setCheckoutUrl(payOSData.getCheckoutUrl());
        saved = transactionRepository.save(saved);

        log.info("PayOS payment created for transaction: {}", saved.getId());

        return saved;
    }

    /**
     * Get transaction by ID
     * @param transactionId Transaction ID
     * @return Transaction
     * @throws ResourceNotFoundException if transaction not found
     */
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> {
                log.warn("Transaction not found: {}", transactionId);
                return new ResourceNotFoundException("Không tìm thấy giao dịch này");
            });
    }

    /**
     * Get all pending transactions
     * @return List of pending transactions ordered by creation date (newest first)
     */
    public List<Transaction> getPendingTransactions() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING);
    }

    /**
     * Get transactions by buyer ID
     * @param buyerId Buyer ID
     * @return List of buyer's transactions
     */
    public List<Transaction> getTransactionsByBuyer(Long buyerId) {
        return transactionRepository.findByBuyerId(buyerId);
    }

    /**
     * Get transactions by seller ID
     * @param sellerId Seller ID
     * @return List of seller's transactions
     */
    public List<Transaction> getTransactionsBySeller(Long sellerId) {
        return transactionRepository.findBySellerId(sellerId);
    }
}
