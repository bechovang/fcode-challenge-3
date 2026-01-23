package com.gameaccountshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

/**
 * PayOS SDK Configuration
 * Creates PayOS bean using official SDK
 * Story 3.1: Buy Now & Show PayOS Payment
 */
@Configuration
public class PayOSSDKConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    /**
     * Create PayOS SDK bean with credentials from application.yml
     * The SDK handles signature generation automatically
     */
    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
