package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {
    List<GameAccount> findBySellerId(Long sellerId);
    List<GameAccount> findByStatus(ListingStatus status);
    List<GameAccount> findByStatusOrderByCreatedAtDesc(ListingStatus status);
}
