package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin top-up approval controller
 * Story 3.1: Wallet System - Admin approves/rejects top-up requests
 */
@Slf4j
@Controller
public class AdminTopUpController {

    private final WalletService walletService;

    public AdminTopUpController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Show all pending top-up requests
     * GET /admin/topups
     */
    @GetMapping("/admin/topups")
    @PreAuthorize("hasRole('ADMIN')")
    public String listPendingTopUps(Model model) {
        List<Transaction> pendingTopUps = walletService.getPendingTopUps();
        model.addAttribute("pendingTopUps", pendingTopUps);
        model.addAttribute("topUpCount", pendingTopUps.size());
        return "admin-topups";
    }

    /**
     * Approve a top-up request
     * POST /admin/topups/{id}/approve
     */
    @PostMapping("/admin/topups/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveTopUp(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            Long adminId = adminUser.getId();
            walletService.approveTopUp(id, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt yêu cầu nạp tiền thành công!");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to approve top-up: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error approving top-up", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
        }

        return "redirect:/admin/topups";
    }

    /**
     * Reject a top-up request
     * POST /admin/topups/{id}/reject
     */
    @PostMapping("/admin/topups/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectTopUp(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            Long adminId = adminUser.getId();

            // Use default reason if not provided
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Không hợp lệ";
            }

            walletService.rejectTopUp(id, adminId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu nạp tiền!");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to reject top-up: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting top-up", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
        }

        return "redirect:/admin/topups";
    }
}
