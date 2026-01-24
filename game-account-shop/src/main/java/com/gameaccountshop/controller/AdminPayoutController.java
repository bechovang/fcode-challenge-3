package com.gameaccountshop.controller;

import com.gameaccountshop.dto.AdminPayoutDto;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.PayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin Payout controller
 * Story 3.4: Admin Payout System
 */
@Controller
@Slf4j
public class AdminPayoutController {

    private final PayoutService payoutService;

    public AdminPayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * Show admin payout management page
     * GET /admin/payouts
     */
    @GetMapping("/admin/payouts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showPayouts(@RequestParam(required = false) String tab, Model model) {
        log.info("Admin viewing payout page, tab: {}", tab);

        List<AdminPayoutDto> needsPayment = payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);
        List<AdminPayoutDto> paid = payoutService.getPayoutsByStatus(PayoutStatus.PAID);
        List<AdminPayoutDto> received = payoutService.getPayoutsByStatus(PayoutStatus.RECEIVED);

        model.addAttribute("needsPayment", needsPayment);
        model.addAttribute("paid", paid);
        model.addAttribute("received", received);
        model.addAttribute("activeTab", tab);

        return "admin-payouts";
    }

    /**
     * Mark payout as PAID
     * POST /admin/payouts/{payoutId}/mark-paid
     */
    @PostMapping("/admin/payouts/{payoutId}/mark-paid")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String markAsPaid(
            @PathVariable Long payoutId,
            @AuthenticationPrincipal CustomUserDetails adminDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long adminId = adminDetails.getId();
            payoutService.markAsPaid(payoutId, adminId);

            redirectAttributes.addFlashAttribute("successMessage",
                "Đã đánh dấu thanh toán. Email đã được gửi cho người bán.");

            return "redirect:/admin/payouts";

        } catch (Exception e) {
            log.error("Error marking payout as paid: {}", payoutId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Lỗi khi xác nhận thanh toán. Vui lòng thử lại.");
            return "redirect:/admin/payouts";
        }
    }
}
