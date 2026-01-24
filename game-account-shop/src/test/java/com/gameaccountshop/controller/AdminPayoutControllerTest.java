package com.gameaccountshop.controller;

import com.gameaccountshop.dto.AdminPayoutDto;
import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPayoutControllerTest {

    @Mock
    private PayoutService payoutService;

    @InjectMocks
    private AdminPayoutController controller;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private CustomUserDetails adminDetails;
    private List<AdminPayoutDto> needsPaymentList;
    private List<AdminPayoutDto> paidList;
    private List<AdminPayoutDto> receivedList;

    @BeforeEach
    void setUp() {
        adminDetails = new CustomUserDetails(
            "admin",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")),
            2L
        );

        AdminPayoutDto payout1 = new AdminPayoutDto(
            1L,
            1L,
            "testseller",
            "seller@example.com",
            new BigDecimal("900000"),
            "NEEDS_PAYMENT",
            "Chờ thanh toán",
            LocalDateTime.now(),
            null,
            null,
            null
        );

        AdminPayoutDto payout2 = new AdminPayoutDto(
            2L,
            1L,
            "testseller",
            "seller@example.com",
            new BigDecimal("450000"),
            "PAID",
            "Đã chuyển",
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            2L
        );

        needsPaymentList = Arrays.asList(payout1);
        paidList = Arrays.asList(payout2);
        receivedList = Collections.emptyList();
    }

    @Test
    void testShowPayouts_GroupedByStatus() {
        // Given
        when(payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT))
            .thenReturn(needsPaymentList);
        when(payoutService.getPayoutsByStatus(PayoutStatus.PAID))
            .thenReturn(paidList);
        when(payoutService.getPayoutsByStatus(PayoutStatus.RECEIVED))
            .thenReturn(receivedList);

        // When
        String viewName = controller.showPayouts(null, model);

        // Then
        assertEquals("admin-payouts", viewName);
        verify(payoutService).getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);
        verify(payoutService).getPayoutsByStatus(PayoutStatus.PAID);
        verify(payoutService).getPayoutsByStatus(PayoutStatus.RECEIVED);
        verify(model).addAttribute("needsPayment", needsPaymentList);
        verify(model).addAttribute("paid", paidList);
        verify(model).addAttribute("received", receivedList);
    }

    @Test
    void testMarkAsPaid_Success() {
        // Given
        Long payoutId = 1L;
        doNothing().when(payoutService).markAsPaid(eq(payoutId), eq(2L));

        // When
        String result = controller.markAsPaid(payoutId, adminDetails, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/payouts", result);
        verify(payoutService).markAsPaid(eq(payoutId), eq(2L));
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void testMarkAsPaid_PayoutNotFound_ReturnsErrorMessage() {
        // Given
        Long payoutId = 999L;
        doThrow(new IllegalArgumentException("Payout not found"))
            .when(payoutService).markAsPaid(eq(payoutId), any());

        // When
        String result = controller.markAsPaid(payoutId, adminDetails, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/payouts", result);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), any());
    }

    @Test
    void testMarkAsPaid_WrongStatus_ReturnsErrorMessage() {
        // Given
        Long payoutId = 1L;
        doThrow(new IllegalStateException("Payout is not in NEEDS_PAYMENT status"))
            .when(payoutService).markAsPaid(eq(payoutId), any());

        // When
        String result = controller.markAsPaid(payoutId, adminDetails, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/payouts", result);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), any());
    }

    @Test
    void testShowPayouts_WithTabParameter() {
        // Given
        when(payoutService.getPayoutsByStatus(any())).thenReturn(Collections.emptyList());

        // When
        String viewName = controller.showPayouts("paid", model);

        // Then
        assertEquals("admin-payouts", viewName);
        verify(model).addAttribute("activeTab", "paid");
    }
}
