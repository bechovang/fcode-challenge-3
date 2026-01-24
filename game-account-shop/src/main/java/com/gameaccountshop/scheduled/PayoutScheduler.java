package com.gameaccountshop.scheduled;

import com.gameaccountshop.service.PayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for payout system
 * Story 3.4: Admin Payout System
 */
@Component
@Slf4j
public class PayoutScheduler {

    private final PayoutService payoutService;

    public PayoutScheduler(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * Create monthly NEEDS_PAYMENT payouts
     * Runs at midnight (00:00) on the 1st of every month
     * Cron: 0 0 0 1 * ?
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void createMonthlyPayouts() {
        log.info("Scheduled task: Creating monthly payouts");
        try {
            payoutService.createMonthlyPayouts();
        } catch (Exception e) {
            log.error("Error creating monthly payouts", e);
        }
    }
}
