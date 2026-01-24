package com.gameaccountshop.controller;

import com.gameaccountshop.dto.MyListingDto;
import com.gameaccountshop.dto.SellerPayoutSummaryDto;
import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.GameAccountService;
import com.gameaccountshop.service.PayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * My Listings controller
 * Story 3.3: My Listings - Filtering & Profit Display
 * Story 3.4: Admin Payout System - Received Payment button
 */
@Controller
@Slf4j
public class MyListingsController {

    private final GameAccountService gameAccountService;
    private final PayoutService payoutService;

    public MyListingsController(GameAccountService gameAccountService, PayoutService payoutService) {
        this.gameAccountService = gameAccountService;
        this.payoutService = payoutService;
    }

    /**
     * Show seller's listings with status filter and pending payouts
     * GET /my-listings
     */
    @GetMapping("/my-listings")
    @PreAuthorize("isAuthenticated()")
    public String showMyListings(
            @RequestParam(required = false) String status,
            Authentication authentication,
            Model model) {

        // Get current user ID from authentication principal
        // Note: CustomUserDetails stores user ID as getId()
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("Authentication principal cannot be null");
        }

        Long userId;
        if (principal instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) principal).getId();
        } else if (principal instanceof Long) {
            // Direct Long principal (fallback for testing scenarios)
            userId = (Long) principal;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        log.debug("User {} viewing my-listings with status filter: {}", userId, status);

        // Fetch listings based on status filter (returns DTO)
        List<MyListingDto> listings = gameAccountService.findMyListings(userId, status);

        // Calculate profit from SOLD listings (total - 10% commission)
        Long profit = gameAccountService.calculateProfit(userId);

        // Story 3.4: Add pending payouts for "Received" button
        List<Payout> pendingPayouts = payoutService.getPendingPayoutsForSeller(userId);
        SellerPayoutSummaryDto payoutSummary = payoutService.getSellerPayoutSummary(userId);

        // Add data to model
        model.addAttribute("listings", listings);
        model.addAttribute("profit", profit);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingPayouts", pendingPayouts);
        model.addAttribute("payoutSummary", payoutSummary);

        return "my-listings";
    }

    /**
     * Confirm payout receipt (seller action)
     * Story 3.4: Admin Payout System
     * POST /my-listings/payouts/{payoutId}/confirm-received
     */
    @PostMapping("/my-listings/payouts/{payoutId}/confirm-received")
    @PreAuthorize("isAuthenticated()")
    public String confirmReceived(
            @PathVariable Long payoutId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long sellerId = userDetails.getId();
            payoutService.markAsReceived(payoutId, sellerId);

            redirectAttributes.addFlashAttribute("successMessage",
                "Đã xác nhận nhận tiền. Cảm ơn bạn!");

            return "redirect:/my-listings";

        } catch (Exception e) {
            log.error("Error confirming payout receipt: {}", payoutId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Lỗi khi xác nhận. Vui lòng thử lại.");
            return "redirect:/my-listings";
        }
    }
}
