package com.gameaccountshop.dto;

import lombok.Data;

/**
 * PayOS payment response DTO
 * Story 3.1: Buy Now & Show PayOS Payment
 */
@Data
public class PayOSResponse {
    private String code;
    private String desc;
    private PayOSData data;

    @Data
    public static class PayOSData {
        private String qrCode;
        private String checkoutUrl;
        private String paymentLinkId;
        private Integer orderCode;
        private Integer amount;
        private String status;
    }
}
