package com.gameaccountshop.repository;

import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // Existing methods from Story 2.1
    List<GameAccount> findBySellerId(Long sellerId);
    List<GameAccount> findByStatus(ListingStatus status);
    List<GameAccount> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    // NEW for Story 2.2: Find pending listings (oldest first - FIFO for Story 2.4)
    List<GameAccount> findByStatusOrderByCreatedAtAsc(ListingStatus status);

    // NEW for Story 2.2: Filter by account rank (e.g., "Gold", "Diamond") with ORDER BY
    @Query("SELECT g FROM GameAccount g WHERE g.accountRank = :rank AND g.status = :status ORDER BY g.createdAt DESC")
    List<GameAccount> findByStatusAndAccountRank(@Param("rank") String rank, @Param("status") ListingStatus status);

    // NEW for Story 2.2: Search by game name (case-insensitive SQL LIKE) with ORDER BY
    @Query("SELECT g FROM GameAccount g WHERE LOWER(g.gameName) LIKE CONCAT('%', LOWER(:search), '%') AND g.status = :status ORDER BY g.createdAt DESC")
    List<GameAccount> findByGameNameContainingAndStatus(@Param("search") String search, @Param("status") ListingStatus status);
}
