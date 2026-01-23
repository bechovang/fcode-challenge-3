package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wallet controller
 * Story 3.1: Wallet System - Wallet page and balance display
 */
@Slf4j
@Controller
public class WalletController {

    private final WalletService walletService;
    private final com.gameaccountshop.repository.TransactionRepository transactionRepository;

    public WalletController(WalletService walletService,
                            com.gameaccountshop.repository.TransactionRepository transactionRepository) {
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Add wallet balance to all models for navbar display
     */
    @ModelAttribute(name = "walletBalance")
    public BigDecimal addWalletBalanceToModel(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return walletService.getBalance(userDetails.getId());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Show wallet page with transaction history
     * GET /wallet
     */
    @GetMapping("/wallet")
    @PreAuthorize("isAuthenticated()")
    public String showWallet(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();
        BigDecimal balance = walletService.getBalance(userId);

        // Get user's transaction history
        List<Transaction> transactions = transactionRepository.findByBuyerIdOrderByCreatedAtDesc(userId);

        model.addAttribute("balance", balance);
        model.addAttribute("transactions", transactions);
        model.addAttribute("transactionCount", transactions.size());

        return "wallet";
    }
}
