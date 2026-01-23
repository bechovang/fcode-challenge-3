package com.gameaccountshop.dto;

import lombok.Data;

import java.util.List;

/**
 * PayOS payment request DTO
 * Story 3.1: Buy Now & Show PayOS Payment
 */
@Data
public class PayOSRequest {
    private Long orderCode;  // Changed from Integer to Long to prevent overflow
    private Integer amount;
    private String description;
    private String cancelUrl;
    private String returnUrl;
    private List<PayOSItem> items;

    @Data
    public static class PayOSItem {
        private String name;
        private Integer quantity;
        private Integer price;
    }
}
