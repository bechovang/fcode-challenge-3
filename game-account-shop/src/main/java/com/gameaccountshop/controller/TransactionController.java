package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Transaction controller
 * Story 3.1: Buy Now - Opens PayOS in new tab, shows notification on current page
 */
@Slf4j
@Controller
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create transaction and show pending notification page
     * POST /listings/{id}/buy
     * Story 3.1: Buy Now - Opens PayOS in new tab, shows notification on current page
     */
    @PostMapping("/listings/{id}/buy")
    @PreAuthorize("isAuthenticated()")
    public String buyListing(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Get buyer ID directly from CustomUserDetails (no DB query needed)
            Long buyerId = userDetails.getId();

            log.info("User {} initiating purchase for listing {}", buyerId, id);

            // Create transaction (this also creates PayOS payment link)
            Transaction transaction = transactionService.createTransaction(id, buyerId);

            // Store transaction ID in session for webhook/reference
            session.setAttribute("lastTransactionId", transaction.getId());

            // Get PayOS checkout URL from transaction
            String checkoutUrl = transaction.getCheckoutUrl();

            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                log.error("Checkout URL is null for transaction: {}", transaction.getId());
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo link thanh toán. Vui lòng thử lại.");
                return "redirect:/listings/" + id;
            }

            log.info("Transaction created: {}, checkoutUrl: {}", transaction.getId(), checkoutUrl);

            // Store checkout URL in session for the pending page
            session.setAttribute("checkoutUrl", checkoutUrl);

            // Show pending notification page (which will open PayOS in new tab)
            return "redirect:/purchase-pending";

        } catch (IllegalStateException e) {
            log.warn("Purchase failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/" + id;
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/listings/" + id;
        }
    }

    /**
     * Show purchase pending notification page
     * GET /purchase-pending
     */
    @GetMapping("/purchase-pending")
    public String showPurchasePending(HttpSession session, Model model) {
        String checkoutUrl = (String) session.getAttribute("checkoutUrl");
        if (checkoutUrl != null) {
            model.addAttribute("checkoutUrl", checkoutUrl);
        }
        return "purchase-pending";
    }

    /**
     * Payment success page - PayOS redirects here after successful payment
     * GET /payment/success
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(HttpSession session) {
        Long transactionId = (Long) session.getAttribute("lastTransactionId");
        log.info("Payment successful for transaction: {}", transactionId);
        return "payment-success";
    }

    /**
     * Payment cancel page - PayOS redirects here when user cancels
     * GET /payment/cancel
     */
    @GetMapping("/payment/cancel")
    public String paymentCancel(HttpSession session) {
        Long transactionId = (Long) session.getAttribute("lastTransactionId");
        log.info("Payment cancelled for transaction: {}", transactionId);
        return "payment-cancel";
    }
}
