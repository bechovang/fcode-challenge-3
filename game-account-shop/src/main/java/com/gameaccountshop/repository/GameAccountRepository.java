package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for GameAccount entity
 * Provides data access methods for game account listings
 */
@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    /**
     * Find all listings by seller ID
     * @param sellerId the seller's user ID
     * @return list of game accounts belonging to the seller
     */
    List<GameAccount> findBySellerId(Long sellerId);

    /**
     * Find all listings by status
     * @param status the account status
     * @return list of game accounts with the specified status
     */
    List<GameAccount> findByStatus(AccountStatus status);

    /**
     * Find approved listings by game name (search)
     * @param gameName the game name to search for (supports LIKE)
     * @return list of matching approved game accounts
     */
    @Query("SELECT g FROM GameAccount g WHERE g.status = :status AND g.gameName LIKE CONCAT('%', :gameName, '%')")
    List<GameAccount> findApprovedByGameName(@Param("gameName") String gameName, @Param("status") AccountStatus status);

    /**
     * Find approved listings by rank (filter)
     * @param rank the rank to filter by
     * @param status the account status (usually APPROVED)
     * @return list of game accounts with the specified rank
     */
    List<GameAccount> findByStatusAndRank(AccountStatus status, String rank);
}
