package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Transaction repository
 * Story 3.1: Wallet System & Buy with Balance
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerId(Long buyerId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    List<Transaction> findBySellerId(Long sellerId);

    /**
     * Find transactions by user ID (as buyer)
     */
    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    /**
     * Find pending top-up transactions
     */
    List<Transaction> findByStatusAndTransactionTypeOrderByCreatedAtDesc(
            TransactionStatus status,
            TransactionType transactionType
    );
}
