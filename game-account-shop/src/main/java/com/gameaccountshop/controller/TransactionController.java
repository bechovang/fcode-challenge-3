package com.gameaccountshop.controller;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.TransactionRepository;
import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.EmailService;
import com.gameaccountshop.service.WalletService;
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

import java.math.BigDecimal;

/**
 * Transaction controller
 * Story 3.1: Wallet System - Buy with wallet balance
 */
@Slf4j
@Controller
public class TransactionController {

    private final WalletService walletService;
    private final EmailService emailService;
    private final GameAccountRepository gameAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(WalletService walletService,
                                  EmailService emailService,
                                  GameAccountRepository gameAccountRepository,
                                  TransactionRepository transactionRepository,
                                  UserRepository userRepository) {
        this.walletService = walletService;
        this.emailService = emailService;
        this.gameAccountRepository = gameAccountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Buy listing with wallet balance
     * POST /listings/{id}/buy
     * Story 3.1: Buy with wallet balance - immediate deduction, email sent
     */
    @PostMapping("/listings/{id}/buy")
    @PreAuthorize("isAuthenticated()")
    public String buyListing(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Long buyerId = userDetails.getId();

            log.info("User {} initiating purchase for listing {}", buyerId, id);

            // Get listing
            GameAccount listing = gameAccountRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản này"));

            // Validate listing status
            if (listing.getStatus() != ListingStatus.APPROVED) {
                throw new IllegalStateException("Tài khoản này hiện không có sẵn để mua");
            }

            // Prevent buying own listing
            if (listing.getSellerId().equals(buyerId)) {
                throw new IllegalStateException("Bạn không thể mua tài khoản của chính mình");
            }

            // Calculate amounts
            // Buyer pays exact listing price (no extra commission)
            // Commission is deducted from seller's earnings
            BigDecimal listingPrice = new BigDecimal(listing.getPrice());
            BigDecimal commission = listingPrice.multiply(new BigDecimal("0.10"));
            BigDecimal totalAmount = listingPrice; // Buyer pays only listing price

            // Check wallet balance
            if (!walletService.hasBalance(buyerId, totalAmount)) {
                BigDecimal currentBalance = walletService.getBalance(buyerId);
                log.warn("Insufficient balance for user {}: has={}, needs={}", buyerId, currentBalance, totalAmount);
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Số dư không đủ. Bạn cần " + formatMoney(totalAmount) + " VNĐ nhưng chỉ có " + formatMoney(currentBalance) + " VNĐ. " +
                    "<a href='/wallet/topup' style='color: #3498db; font-weight: bold;'>Nạp thêm tiền</a>");
                return "redirect:/listings/" + id;
            }

            // Deduct balance from wallet
            walletService.deductBalance(buyerId, totalAmount);

            // Mark listing as SOLD
            listing.setStatus(ListingStatus.SOLD);
            listing.setSoldAt(java.time.LocalDateTime.now());
            gameAccountRepository.save(listing);
            log.info("Listing marked as SOLD: id={}", id);

            // Create PURCHASE transaction
            Transaction transaction = new Transaction();
            transaction.setListingId(id);
            transaction.setBuyerId(buyerId);
            transaction.setSellerId(listing.getSellerId());
            transaction.setAmount(listingPrice);
            transaction.setCommission(commission);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setTransactionType(TransactionType.PURCHASE);
            transaction = transactionRepository.save(transaction);

            log.info("Transaction completed: {}", transaction.getId());

            // Send account credentials via email immediately
            User buyer = userRepository.findById(buyerId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy người mua"));
            emailService.sendAccountCredentialsEmail(
                    buyer.getEmail(),
                    listing.getGameName(),
                    listing.getAccountRank(),
                    listing.getAccountUsername(),
                    listing.getAccountPassword(),
                    listing.getDescription()
            );

            // Store transaction info in session for success page
            session.setAttribute("purchaseTransactionId", transaction.getId());
            session.setAttribute("purchaseGameName", listing.getGameName());
            session.setAttribute("purchaseAmount", listingPrice);

            return "redirect:/purchase-success";

        } catch (IllegalStateException e) {
            log.warn("Purchase failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/" + id;
        } catch (Exception e) {
            log.error("Error processing purchase", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/listings/" + id;
        }
    }

    /**
     * Purchase success page
     * GET /purchase-success
     */
    @GetMapping("/purchase-success")
    @PreAuthorize("isAuthenticated()")
    public String purchaseSuccess(HttpSession session, Model model) {
        Long transactionId = (Long) session.getAttribute("purchaseTransactionId");
        String gameName = (String) session.getAttribute("purchaseGameName");
        BigDecimal amount = (BigDecimal) session.getAttribute("purchaseAmount");

        if (transactionId != null) {
            model.addAttribute("transactionId", transactionId);
        }
        if (gameName != null) {
            model.addAttribute("gameName", gameName);
        }
        if (amount != null) {
            model.addAttribute("amount", amount);
        }

        return "purchase-success";
    }

    /**
     * Format money with thousand separators
     */
    private String formatMoney(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }
}
