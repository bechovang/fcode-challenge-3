package com.gameaccountshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.math.BigDecimal;

/**
 * PayOS payment service using official SDK
 * Story 3.1: Buy Now & Show PayOS Payment
 *
 * Uses vn.payos.PayOS SDK which handles:
 * - Automatic signature generation
 * - Proper request serialization
 * - Type-safe response handling
 */
@Service
@Slf4j
public class PayOSService {

    private final PayOS payOS;

    public PayOSService(PayOS payOS) {
        this.payOS = payOS;
    }

    /**
     * Create a payment request with PayOS using official SDK
     *
     * @param amount Total amount to pay (listing price + commission)
     * @param transactionId Transaction ID for logging
     * @param description Payment description (must be <= 25 characters)
     * @return PayOSData containing QR code, checkout URL, and payment link ID
     * @throws RuntimeException if payment creation fails
     */
    public PayOSData createPayment(BigDecimal amount, String transactionId, String description) {
        try {
            // Generate unique order code (seconds since epoch)
            long orderCode = System.currentTimeMillis() / 1000;

            // Convert amount to long (PayOS uses long, not int)
            long amountLong = amount.longValue();

            // ==================== DEBUG INFO ====================
            System.out.println("\n==================== PAYOS SDK CREATE PAYMENT ====================");
            System.out.println("OrderCode: " + orderCode);
            System.out.println("Amount: " + amountLong);
            System.out.println("Description: '" + description + "' (length: " + description.length() + ")");
            System.out.println("ReturnUrl: http://localhost:8080/payment/success");
            System.out.println("CancelUrl: http://localhost:8080/payment/cancel");
            System.out.println("===============================================================\n");
            // =========================================================

            // Build payment request using PayOS SDK builder pattern
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name("Tai khoan game")
                    .quantity(1)
                    .price(amountLong)
                    .build();

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amountLong)
                    .description(description)
                    .item(item)  // Note: singular "item", not "items"
                    .returnUrl("http://localhost:8080/payment/success")
                    .cancelUrl("http://localhost:8080/payment/cancel")
                    .build();

            // Call PayOS API using SDK (handles signature automatically)
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            // ==================== DEBUG RESPONSE ====================
            System.out.println("\n==================== PAYOS SDK RESPONSE ====================");
            System.out.println("Success! Payment link created");
            System.out.println("Payment Link ID: " + response.getPaymentLinkId());
            System.out.println("Checkout URL: " + response.getCheckoutUrl());
            System.out.println("QR Code present: " + (response.getQrCode() != null));
            System.out.println("===========================================================\n");
            // =========================================================

            log.info("PayOS payment created successfully for transaction: {}", transactionId);

            // Return simplified data object
            return new PayOSData(
                    response.getQrCode(),
                    response.getCheckoutUrl(),
                    response.getPaymentLinkId(),
                    orderCode,
                    amountLong
            );

        } catch (Exception e) {
            log.error("Failed to create PayOS payment for transaction: {}", transactionId, e);
            System.out.println("\n==================== PAYOS SDK ERROR ====================");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("===========================================================\n");
            throw new RuntimeException("Không thể tạo yêu cầu thanh toán PayOS: " + e.getMessage(), e);
        }
    }

    /**
     * Check payment status by order code
     *
     * @param orderCode The order code from payment creation
     * @return Payment status (PENDING, PAID, CANCELLED, EXPIRED)
     */
    public String checkPaymentStatus(Long orderCode) {
        try {
            var paymentLink = payOS.paymentRequests().get(orderCode);
            return paymentLink.getStatus().toString();
        } catch (Exception e) {
            log.error("Failed to check PayOS payment status for orderCode: {}", orderCode, e);
            return "ERROR";
        }
    }

    /**
     * Cancel a payment link
     *
     * @param orderCode The order code to cancel
     * @param reason Cancellation reason
     * @return true if cancelled successfully
     */
    public boolean cancelPayment(Long orderCode, String reason) {
        try {
            payOS.paymentRequests().cancel(orderCode, reason);
            log.info("Cancelled payment link for orderCode: {}", orderCode);
            return true;
        } catch (Exception e) {
            log.error("Failed to cancel payment for orderCode: {}", orderCode, e);
            return false;
        }
    }

    /**
     * Simple data holder for PayOS response
     * Contains the essential data needed after payment creation
     */
    public static class PayOSData {
        private final String qrCode;
        private final String checkoutUrl;
        private final String paymentLinkId;
        private final Long orderCode;
        private final Long amount;

        public PayOSData(String qrCode, String checkoutUrl, String paymentLinkId, Long orderCode, Long amount) {
            this.qrCode = qrCode;
            this.checkoutUrl = checkoutUrl;
            this.paymentLinkId = paymentLinkId;
            this.orderCode = orderCode;
            this.amount = amount;
        }

        public String getQrCode() {
            return qrCode;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public String getPaymentLinkId() {
            return paymentLinkId;
        }

        public Long getOrderCode() {
            return orderCode;
        }

        public Long getAmount() {
            return amount;
        }
    }
}
