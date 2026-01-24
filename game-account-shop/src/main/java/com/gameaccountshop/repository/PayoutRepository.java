package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Payout> findByStatusOrderByCreatedAtDesc(PayoutStatus status);

    List<Payout> findBySellerIdAndStatus(Long sellerId, PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.sellerId = :sellerId AND p.status = :status")
    BigDecimal sumAmountBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                           @Param("status") PayoutStatus status);

    @Query("SELECT COUNT(p) > 0 FROM Payout p WHERE p.sellerId = :sellerId AND p.status = :status AND MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    boolean existsBySellerIdAndStatusAndMonth(@Param("sellerId") Long sellerId,
                                              @Param("status") PayoutStatus status,
                                              @Param("month") int month,
                                              @Param("year") int year);
}
