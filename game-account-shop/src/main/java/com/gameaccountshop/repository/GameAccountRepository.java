package com.gameaccountshop.repository;

import com.gameaccountshop.dto.ListingDetailDto;
import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // Existing methods from Story 2.1
    List<GameAccount> findBySellerId(Long sellerId);
    List<GameAccount> findByStatus(ListingStatus status);
    List<GameAccount> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    // Story 3.3: My Listings - Filtering & Profit
    List<GameAccount> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<GameAccount> findBySellerIdAndStatus(Long sellerId, ListingStatus status);

    @Query("SELECT COALESCE(SUM(g.price), 0) FROM GameAccount g WHERE g.sellerId = :sellerId AND g.status = :status")
    Long sumPriceBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                     @Param("status") ListingStatus status);

    // NEW for Story 2.2: Find pending listings (oldest first - FIFO for Story 2.4)
    List<GameAccount> findByStatusOrderByCreatedAtAsc(ListingStatus status);

    // NEW for Story 2.2 Improvements: Robust search with aliases (handled in Service), content search, rank filter, and sorting
    @Query("SELECT g FROM GameAccount g WHERE g.status = :status " +
           "AND (:rank IS NULL OR g.accountRank LIKE CONCAT(:rank, '%')) " +
           "AND (:search IS NULL OR (" +
           "   LOWER(g.gameName) LIKE CONCAT('%', LOWER(:search), '%') " +
           "   OR LOWER(g.description) LIKE CONCAT('%', LOWER(:search), '%') " +
           "   OR LOWER(g.accountRank) LIKE CONCAT('%', LOWER(:search), '%') " +
           "   OR g.sellerId IN (SELECT u.id FROM User u WHERE LOWER(u.username) LIKE CONCAT('%', LOWER(:search), '%'))" +
           "))")
    List<GameAccount> findApprovedListings(@Param("search") String search,
                                           @Param("rank") String rank,
                                           @Param("status") ListingStatus status,
                                           Sort sort);

    // OLD methods kept for reference or legacy compatibility if needed, but the new one supersedes them for the main page
    // NEW for Story 2.2: Filter by account rank (e.g., "Gold", "Diamond") with ORDER BY
    @Query("SELECT g FROM GameAccount g WHERE g.accountRank = :rank AND g.status = :status ORDER BY g.createdAt DESC")
    List<GameAccount> findByStatusAndAccountRank(@Param("rank") String rank, @Param("status") ListingStatus status);

    // NEW for Story 2.2: Search by game name (case-insensitive SQL LIKE) with ORDER BY
    @Query("SELECT g FROM GameAccount g WHERE LOWER(g.gameName) LIKE CONCAT('%', LOWER(:search), '%') AND g.status = :status ORDER BY g.createdAt DESC")
    List<GameAccount> findByGameNameContainingAndStatus(@Param("search") String search, @Param("status") ListingStatus status);

    // NEW for Story 2.3: Find a specific listing by ID with seller information
    // Only returns APPROVED or SOLD listings (PENDING/REJECTED return empty Optional)
    @Query("SELECT new com.gameaccountshop.dto.ListingDetailDto(" +
           "g.id, g.gameName, g.accountRank, g.price, g.description, " +
           "g.status, g.createdAt, g.soldAt, " +
           "u.id, u.username, u.email) " +
           "FROM GameAccount g " +
           "LEFT JOIN User u ON g.sellerId = u.id " +
           "WHERE g.id = :id " +
           "  AND g.status IN ('APPROVED', 'SOLD')")
    Optional<ListingDetailDto> findDetailById(@Param("id") Long id);

    // Story 3.4: Payout System - Get distinct seller IDs who have sold listings
    @Query("SELECT DISTINCT g.sellerId FROM GameAccount g WHERE g.status = :status")
    List<Long> findDistinctSellerIdsByStatus(@Param("status") ListingStatus status);
}
