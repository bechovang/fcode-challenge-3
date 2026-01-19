package com.gameaccountshop.controller;

import com.gameaccountshop.dto.AdminListingDto;
import com.gameaccountshop.service.GameAccountService;
import com.gameaccountshop.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin controller for managing listing approval workflow
 * Story 2.4: Admin Approve/Reject Listings
 * All endpoints require ADMIN role
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GameAccountService gameAccountService;

    public AdminController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Admin review page - show all pending listings
     * GET /admin/review
     * Displays pending listings in FIFO order (oldest first)
     */
    @GetMapping("/review")
    public String reviewListings(Model model) {
        log.info("Admin viewing pending listings review page");
        List<AdminListingDto> pendingListings = gameAccountService.findPendingListings();
        model.addAttribute("listings", pendingListings);
        return "admin/review";
    }

    /**
     * Approve a listing
     * POST /admin/review/{id}/approve
     * Updates listing status to APPROVED and redirects back to review page
     */
    @PostMapping("/review/{id}/approve")
    public String approveListing(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        log.info("Admin approving listing: id={}", id);
        try {
            gameAccountService.approveListing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt tài khoản");
        } catch (ResourceNotFoundException e) {
            log.warn("Listing not found for approval: id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid approval attempt: id={}, error={}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error approving listing: id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi duyệt tài khoản");
        }
        return "redirect:/admin/review";
    }

    /**
     * Reject a listing
     * POST /admin/review/{id}/reject
     * Updates listing status to REJECTED with reason and redirects back to review page
     */
    @PostMapping("/review/{id}/reject")
    public String rejectListing(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = true) String reason,
            RedirectAttributes redirectAttributes) {
        log.info("Admin rejecting listing: id={}, reason={}", id, reason);

        // Validate reason is not blank
        if (reason == null || reason.isBlank()) {
            log.warn("Reject reason is blank: id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/review";
        }

        try {
            gameAccountService.rejectListing(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối tài khoản");
        } catch (ResourceNotFoundException e) {
            log.warn("Listing not found for rejection: id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid rejection attempt: id={}, error={}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting listing: id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi từ chối tài khoản");
        }
        return "redirect:/admin/review";
    }

    /**
     * Mark a listing as sold
     * Story 2.5: Mark Listing as Sold
     * POST /admin/listings/{id}/mark-sold
     * Updates listing status to SOLD and redirects to detail page
     */
    @PostMapping("/listings/{id}/mark-sold")
    public String markAsSold(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        log.info("Admin marking listing as sold: id={}", id);
        try {
            gameAccountService.markAsSold(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu tài khoản đã bán");
        } catch (ResourceNotFoundException e) {
            log.warn("Listing not found for mark-as-sold: id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid mark-as-sold attempt: id={}, error={}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error marking listing as sold: id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đánh dấu đã bán");
        }
        return "redirect:/listings/" + id;
    }
}
