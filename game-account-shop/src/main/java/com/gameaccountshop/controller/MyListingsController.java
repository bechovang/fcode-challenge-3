package com.gameaccountshop.controller;

import com.gameaccountshop.dto.MyListingDto;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.GameAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * My Listings controller
 * Story 3.3: My Listings - Filtering & Profit Display
 */
@Controller
@Slf4j
public class MyListingsController {

    private final GameAccountService gameAccountService;

    public MyListingsController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Show seller's listings with status filter
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

        // Add data to model
        model.addAttribute("listings", listings);
        model.addAttribute("profit", profit);
        model.addAttribute("selectedStatus", status);

        return "my-listings";
    }
}
