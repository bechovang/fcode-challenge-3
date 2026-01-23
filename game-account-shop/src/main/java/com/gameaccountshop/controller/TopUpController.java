package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.TransactionService;
import com.gameaccountshop.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Top-up controller
 * Story 3.1: Wallet System - Handle wallet top-up requests
 */
@Slf4j
@Controller
public class TopUpController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    public TopUpController(WalletService walletService,
                           TransactionService transactionService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    /**
     * Show top-up page
     * GET /wallet/topup
     */
    @GetMapping("/wallet/topup")
    @PreAuthorize("isAuthenticated()")
    public String showTopUpPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();
        BigDecimal currentBalance = walletService.getBalance(userId);

        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("presetAmounts", java.util.List.of(
                new BigDecimal("100000"),
                new BigDecimal("200000"),
                new BigDecimal("500000"),
                new BigDecimal("1000000")
        ));

        return "wallet-topup";
    }

    /**
     * Process top-up request
     * POST /wallet/topup
     */
    @PostMapping("/wallet/topup")
    @PreAuthorize("isAuthenticated()")
    public String processTopUp(
            @RequestParam(required = false) String customAmount,
            @RequestParam(required = false) String presetAmount,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Long userId = userDetails.getId();

            // Parse amount
            BigDecimal amount;
            if (customAmount != null && !customAmount.trim().isEmpty()) {
                try {
                    amount = new BigDecimal(customAmount.trim());
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Số tiền không hợp lệ");
                    return "redirect:/wallet/topup";
                }
            } else if (presetAmount != null) {
                amount = new BigDecimal(presetAmount);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập số tiền hoặc chọn mệnh giá");
                return "redirect:/wallet/topup";
            }

            log.info("User {} requesting top-up of: {}", userId, amount);

            // Create top-up transaction
            Transaction transaction = walletService.createTopUpTransaction(userId, amount, transactionService);

            // Store transaction ID in session
            session.setAttribute("lastTopUpTransactionId", transaction.getId());
            session.setAttribute("topUpQrCode", transaction.getQrCode());
            session.setAttribute("topUpCheckoutUrl", transaction.getCheckoutUrl());
            session.setAttribute("topUpAmount", amount);

            return "redirect:/wallet/topup/pending";

        } catch (IllegalArgumentException e) {
            log.warn("Top-up validation failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wallet/topup";
        } catch (Exception e) {
            log.error("Error creating top-up transaction", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/wallet/topup";
        }
    }

    /**
     * Show top-up pending page with QR code
     * GET /wallet/topup/pending
     */
    @GetMapping("/wallet/topup/pending")
    @PreAuthorize("isAuthenticated()")
    public String showTopUpPending(HttpSession session, Model model) {
        String qrCode = (String) session.getAttribute("topUpQrCode");
        String checkoutUrl = (String) session.getAttribute("topUpCheckoutUrl");
        BigDecimal amount = (BigDecimal) session.getAttribute("topUpAmount");

        if (qrCode != null) {
            model.addAttribute("qrCode", qrCode);
        }
        if (checkoutUrl != null) {
            model.addAttribute("checkoutUrl", checkoutUrl);
        }
        if (amount != null) {
            model.addAttribute("amount", amount);
        }

        return "wallet-topup-pending";
    }

    /**
     * Top-up success page (PayOS redirect)
     * GET /wallet/topup/success
     */
    @GetMapping("/wallet/topup/success")
    @PreAuthorize("isAuthenticated()")
    public String topUpSuccess(HttpSession session) {
        log.info("Top-up payment completed, waiting for admin approval");
        return "wallet-topup-success";
    }

    /**
     * Top-up cancel page (PayOS redirect)
     * GET /wallet/topup/cancel
     */
    @GetMapping("/wallet/topup/cancel")
    @PreAuthorize("isAuthenticated()")
    public String topUpCancel(HttpSession session) {
        log.info("Top-up payment cancelled by user");
        return "wallet-topup-cancel";
    }
}
