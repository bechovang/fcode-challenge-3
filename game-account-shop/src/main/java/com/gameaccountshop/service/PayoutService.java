package com.gameaccountshop.service;

import com.gameaccountshop.dto.AdminPayoutDto;
import com.gameaccountshop.dto.SellerPayoutSummaryDto;
import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.PayoutRepository;
import com.gameaccountshop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payout service
 * Story 3.4: Admin Payout System
 */
@Service
@Slf4j
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PayoutService(PayoutRepository payoutRepository,
                         GameAccountRepository gameAccountRepository,
                         UserRepository userRepository,
                         EmailService emailService) {
        this.payoutRepository = payoutRepository;
        this.gameAccountRepository = gameAccountRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Scheduled task: Create NEEDS_PAYMENT payouts on 1st of month
     * Runs at midnight on the 1st of each month
     */
    @Transactional
    public void createMonthlyPayouts() {
        LocalDate now = LocalDate.now();
        log.info("Running monthly payout creation for: {}", now);

        // Get all sellers with sold listings
        List<Long> sellerIds = gameAccountRepository.findDistinctSellerIdsByStatus(ListingStatus.SOLD);

        int created = 0;
        for (Long sellerId : sellerIds) {
            // Check if already has NEEDS_PAYMENT for this month
            if (payoutRepository.existsBySellerIdAndStatusAndMonth(
                    sellerId, PayoutStatus.NEEDS_PAYMENT, now.getMonthValue(), now.getYear())) {
                continue;
            }

            // Calculate unpaid amount (total sold - received)
            BigDecimal unpaidAmount = calculateUnpaidEarnings(sellerId);

            if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payout payout = new Payout();
                payout.setSellerId(sellerId);
                payout.setAmount(unpaidAmount);
                payout.setStatus(PayoutStatus.NEEDS_PAYMENT);
                payoutRepository.save(payout);
                created++;
                log.info("Created NEEDS_PAYMENT payout for seller: {}, amount: {}", sellerId, unpaidAmount);
            }
        }

        log.info("Monthly payout creation completed. Created {} payouts.", created);
    }

    /**
     * Calculate unpaid earnings
     * = Total sold (90%) - RECEIVED payouts
     * NOTE: PAID payouts do NOT reduce unpaid earnings
     */
    public BigDecimal calculateUnpaidEarnings(Long sellerId) {
        Long totalSold = gameAccountRepository.sumPriceBySellerIdAndStatus(sellerId, ListingStatus.SOLD);

        if (totalSold == null || totalSold == 0) {
            return BigDecimal.ZERO;
        }

        // Convert Long to BigDecimal and apply 90% commission
        BigDecimal totalSoldDecimal = BigDecimal.valueOf(totalSold);
        BigDecimal netEarnings = totalSoldDecimal.multiply(new BigDecimal("0.90"));

        // Only subtract RECEIVED payouts, NOT PAID payouts
        BigDecimal receivedAmount = payoutRepository.sumAmountBySellerIdAndStatus(sellerId, PayoutStatus.RECEIVED);

        if (receivedAmount == null) {
            receivedAmount = BigDecimal.ZERO;
        }

        return netEarnings.subtract(receivedAmount);
    }

    /**
     * Get payouts grouped by status for admin page
     */
    public List<AdminPayoutDto> getPayoutsByStatus(PayoutStatus status) {
        List<Payout> payouts = payoutRepository.findByStatusOrderByCreatedAtDesc(status);

        // Get seller info for each payout
        List<Long> sellerIds = payouts.stream()
                .map(Payout::getSellerId)
                .distinct()
                .toList();

        Map<Long, User> sellerMap = userRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return payouts.stream()
                .map(p -> AdminPayoutDto.fromEntity(
                        p,
                        sellerMap.getOrDefault(p.getSellerId(), new User()).getUsername(),
                        sellerMap.getOrDefault(p.getSellerId(), new User()).getEmail()
                ))
                .toList();
    }

    /**
     * Get PAID payouts for seller (for "Received" button)
     */
    public List<Payout> getPendingPayoutsForSeller(Long sellerId) {
        return payoutRepository.findBySellerIdAndStatus(sellerId, PayoutStatus.PAID);
    }

    /**
     * Mark payout as PAID (admin action)
     * Sends email to seller
     */
    @Transactional
    public void markAsPaid(Long payoutId, Long adminId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));

        if (payout.getStatus() != PayoutStatus.NEEDS_PAYMENT) {
            throw new IllegalStateException("Payout is not in NEEDS_PAYMENT status");
        }

        payout.setStatus(PayoutStatus.PAID);
        payout.setPaidAt(java.time.LocalDateTime.now());
        payout.setAdminId(adminId);
        payoutRepository.save(payout);

        // Send email to seller
        try {
            User seller = userRepository.findById(payout.getSellerId()).orElse(null);
            if (seller != null) {
                emailService.sendPayoutPaidEmail(
                        seller.getEmail(),
                        payout.getAmount(),
                        "PAYOUT" + payout.getId()
                );
                log.info("Sent payout paid email to seller: {}", seller.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send payout paid email for payout: {}", payoutId, e);
            // Don't throw - payout status is already updated
        }

        log.info("Payout {} marked as PAID by admin {}", payoutId, adminId);
    }

    /**
     * Mark payout as RECEIVED (seller action)
     * Sends email to admin
     */
    @Transactional
    public void markAsReceived(Long payoutId, Long sellerId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));

        if (payout.getStatus() != PayoutStatus.PAID) {
            throw new IllegalStateException("Payout is not in PAID status");
        }

        if (!payout.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("This payout does not belong to you");
        }

        payout.setStatus(PayoutStatus.RECEIVED);
        payout.setReceivedAt(java.time.LocalDateTime.now());
        payoutRepository.save(payout);

        // Send email to admin
        try {
            User seller = userRepository.findById(sellerId).orElse(null);
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (seller != null && admin != null) {
                emailService.sendPayoutReceivedEmail(
                        admin.getEmail(),
                        seller.getUsername(),
                        payout.getAmount()
                );
                log.info("Sent payout received email to admin");
            }
        } catch (Exception e) {
            log.error("Failed to send payout received email for payout: {}", payoutId, e);
            // Don't throw - payout status is already updated
        }

        log.info("Payout {} marked as RECEIVED by seller {}", payoutId, sellerId);
    }

    /**
     * Get seller payout summary for "My Listings" page
     */
    public SellerPayoutSummaryDto getSellerPayoutSummary(Long sellerId) {
        List<Payout> pendingPayouts = getPendingPayoutsForSeller(sellerId);

        BigDecimal totalPendingAmount = pendingPayouts.stream()
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SellerPayoutSummaryDto.PendingPayoutItem> items = pendingPayouts.stream()
                .map(p -> new SellerPayoutSummaryDto.PendingPayoutItem(p.getId(), p.getAmount()))
                .toList();

        return new SellerPayoutSummaryDto(totalPendingAmount, pendingPayouts.size(), items);
    }
}
