package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Wallet repository
 * Story 3.1: Wallet System
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Find wallet by user ID
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Check if wallet exists for user
     */
    boolean existsByUserId(Long userId);
}
