package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.enums.TransactionType;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminTopUpController
 * Tests HTTP request/response handling and controller behavior
 */
@ExtendWith(MockitoExtension.class)
class AdminTopUpControllerTest {

    @Mock
    private WalletService walletService;

    private MockMvc mockMvc;
    private AdminTopUpController controller;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        controller = new AdminTopUpController(walletService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setBuyerId(100L);
        testTransaction.setSellerId(100L);
        testTransaction.setListingId(null);
        testTransaction.setAmount(new BigDecimal("500000"));
        testTransaction.setCommission(BigDecimal.ZERO);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setTransactionType(TransactionType.TOP_UP);
        testTransaction.setQrCode("test-qr-code");
        testTransaction.setCheckoutUrl("https://payos.vn/checkout");
    }

    @Test
    void listPendingTopUps_ShouldReturnViewWithPendingTransactions() {
        // Arrange
        when(walletService.getPendingTopUps()).thenReturn(List.of(testTransaction));

        // Act
        String viewName = controller.listPendingTopUps(mock(org.springframework.ui.Model.class));

        // Assert
        assertThat(viewName).isEqualTo("admin-topups");
        verify(walletService, times(1)).getPendingTopUps();
    }

    @Test
    void approveTopUp_WithValidId_ShouldApproveAndRedirect() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String redirectUrl = controller.approveTopUp(1L, adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(walletService, times(1)).approveTopUp(eq(1L), eq(1L));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void approveTopUp_WithInvalidId_ShouldAddErrorMessageAndRedirect() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new IllegalArgumentException("Transaction not found"))
                .when(walletService).approveTopUp(any(Long.class), any(Long.class));

        // Act
        String redirectUrl = controller.approveTopUp(999L, adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), any());
    }

    @Test
    void rejectTopUp_WithValidIdAndReason_ShouldRejectAndRedirect() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String redirectUrl = controller.rejectTopUp(1L, "Invalid payment", adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(walletService, times(1)).rejectTopUp(eq(1L), eq(1L), eq("Invalid payment"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void rejectTopUp_WithEmptyReason_ShouldUseDefaultReason() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String redirectUrl = controller.rejectTopUp(1L, "", adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(walletService, times(1)).rejectTopUp(eq(1L), eq(1L), eq("Không hợp lệ"));
    }

    @Test
    void rejectTopUp_WithNullReason_ShouldUseDefaultReason() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String redirectUrl = controller.rejectTopUp(1L, null, adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(walletService, times(1)).rejectTopUp(eq(1L), eq(1L), eq("Không hợp lệ"));
    }

    @Test
    void rejectTopUp_WithError_ShouldAddErrorMessageAndRedirect() {
        // Arrange
        CustomUserDetails adminUser = new CustomUserDetails(
                "admin",
                "password",
                List.of(() -> "ROLE_ADMIN"),
                1L
        );
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new IllegalArgumentException("Transaction not found"))
                .when(walletService).rejectTopUp(any(Long.class), any(Long.class), any(String.class));

        // Act
        String redirectUrl = controller.rejectTopUp(999L, "Reason", adminUser, redirectAttributes);

        // Assert
        assertThat(redirectUrl).isEqualTo("redirect:/admin/topups");
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), any());
    }
}
