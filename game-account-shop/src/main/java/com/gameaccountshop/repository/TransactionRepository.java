package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Transaction repository
 * Story 3.1: Buy Now & Show PayOS Payment
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerId(Long buyerId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    List<Transaction> findBySellerId(Long sellerId);
}
