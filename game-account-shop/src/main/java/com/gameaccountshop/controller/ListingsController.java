package com.gameaccountshop.controller;

import com.gameaccountshop.dto.ListingDetailDto;
import com.gameaccountshop.service.GameAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller for listing detail pages (public browse)
 * Story 2.3: Listing Details Page
 * Uses /listings (plural) path for public browsing
 *
 * Exception handling is centralized in GlobalExceptionHandler (@ControllerAdvice)
 */
@Slf4j
@Controller
public class ListingsController {

    private final GameAccountService gameAccountService;

    public ListingsController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Listing detail page
     * GET /listings/{id}
     * Shows full details for APPROVED or SOLD listings only
     * PENDING/REJECTED listings and invalid IDs return 404 via service/repository
     * Exception handling: GlobalExceptionHandler catches:
     *   - NumberFormatException (invalid ID format like "/listings/abc")
     *   - IllegalArgumentException (listing not found or not accessible)
     *
     * @param id Listing ID
     * @param model Spring UI model
     * @return Template name
     */
    @GetMapping("/listings/{id}")
    public String listingDetail(@PathVariable Long id, Model model) {
        log.info("Viewing listing detail: id={}", id);

        ListingDetailDto listing = gameAccountService.getListingDetail(id);
        model.addAttribute("listing", listing);

        return "listing-detail";
    }
}
