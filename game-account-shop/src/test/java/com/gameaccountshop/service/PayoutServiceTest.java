package com.gameaccountshop.service;

import com.gameaccountshop.dto.AdminPayoutDto;
import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.PayoutRepository;
import com.gameaccountshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private GameAccountRepository gameAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PayoutService payoutService;

    private User testSeller;
    private User testAdmin;
    private Payout testPayout;

    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setId(1L);
        testSeller.setUsername("testseller");
        testSeller.setEmail("seller@example.com");
        testSeller.setRole(Role.USER);

        testAdmin = new User();
        testAdmin.setId(2L);
        testAdmin.setUsername("admin");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setRole(Role.ADMIN);

        testPayout = new Payout();
        testPayout.setId(1L);
        testPayout.setSellerId(1L);
        testPayout.setAmount(new BigDecimal("900000"));
        testPayout.setStatus(PayoutStatus.NEEDS_PAYMENT);
    }

    @Test
    void testCreateMonthlyPayouts_CreatesNeedsPaymentRecords() {
        // Given
        LocalDate now = LocalDate.now();
        List<Long> sellerIds = Arrays.asList(1L, 2L);

        when(gameAccountRepository.findDistinctSellerIdsByStatus(ListingStatus.SOLD))
            .thenReturn(sellerIds);
        when(payoutRepository.existsBySellerIdAndStatusAndMonth(eq(1L), eq(PayoutStatus.NEEDS_PAYMENT), anyInt(), anyInt()))
            .thenReturn(false);
        when(payoutRepository.existsBySellerIdAndStatusAndMonth(eq(2L), eq(PayoutStatus.NEEDS_PAYMENT), anyInt(), anyInt()))
            .thenReturn(false);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock calculateUnpaidEarnings for each seller
        when(gameAccountRepository.sumPriceBySellerIdAndStatus(eq(1L), eq(ListingStatus.SOLD)))
            .thenReturn(1000000L); // 1,000,000 * 0.90 = 900,000
        when(gameAccountRepository.sumPriceBySellerIdAndStatus(eq(2L), eq(ListingStatus.SOLD)))
            .thenReturn(500000L);  // 500,000 * 0.90 = 450,000
        when(payoutRepository.sumAmountBySellerIdAndStatus(anyLong(), eq(PayoutStatus.RECEIVED)))
            .thenReturn(BigDecimal.ZERO);

        // When
        payoutService.createMonthlyPayouts();

        // Then
        verify(payoutRepository, times(2)).save(any(Payout.class));
        verify(gameAccountRepository, times(2)).sumPriceBySellerIdAndStatus(anyLong(), eq(ListingStatus.SOLD));
    }

    @Test
    void testCreateMonthlyPayouts_AvoidsDuplicates() {
        // Given
        List<Long> sellerIds = Arrays.asList(1L);
        when(gameAccountRepository.findDistinctSellerIdsByStatus(ListingStatus.SOLD))
            .thenReturn(sellerIds);
        when(payoutRepository.existsBySellerIdAndStatusAndMonth(eq(1L), eq(PayoutStatus.NEEDS_PAYMENT), anyInt(), anyInt()))
            .thenReturn(true); // Already exists for this month

        // When
        payoutService.createMonthlyPayouts();

        // Then - should not create new payout
        verify(payoutRepository, never()).save(any(Payout.class));
    }

    @Test
    void testCalculateUnpaidEarnings_ExcludesReceivedOnly() {
        // Given
        Long sellerId = 1L;
        Long totalSold = 1000000L; // 1,000,000 VND
        BigDecimal receivedAmount = new BigDecimal("450000"); // 450,000 VND received

        when(gameAccountRepository.sumPriceBySellerIdAndStatus(sellerId, ListingStatus.SOLD))
            .thenReturn(totalSold);
        when(payoutRepository.sumAmountBySellerIdAndStatus(sellerId, PayoutStatus.RECEIVED))
            .thenReturn(receivedAmount);
        // Note: PAID payouts are NOT used in calculateUnpaidEarnings calculation

        // When
        BigDecimal result = payoutService.calculateUnpaidEarnings(sellerId);

        // Then: 1,000,000 * 0.90 - 450,000 = 900,000 - 450,000 = 450,000
        assertEquals(0, new BigDecimal("450000").compareTo(result));
        // Verify only RECEIVED was queried
        verify(payoutRepository).sumAmountBySellerIdAndStatus(sellerId, PayoutStatus.RECEIVED);
    }

    @Test
    void testMarkAsPaid_UpdatesStatusAndSendsEmail() {
        // Given
        Long payoutId = 1L;
        Long adminId = 2L;

        when(payoutRepository.findById(payoutId)).thenReturn(Optional.of(testPayout));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
        doNothing().when(emailService).sendPayoutPaidEmail(any(), any(), any());

        // When
        payoutService.markAsPaid(payoutId, adminId);

        // Then
        assertEquals(PayoutStatus.PAID, testPayout.getStatus());
        assertEquals(adminId, testPayout.getAdminId());
        assertNotNull(testPayout.getPaidAt());
        verify(payoutRepository).save(testPayout);
        verify(emailService).sendPayoutPaidEmail(eq("seller@example.com"), eq(new BigDecimal("900000")), any());
    }

    @Test
    void testMarkAsPaid_PayoutNotInNeedsPaymentStatus_ThrowsException() {
        // Given
        testPayout.setStatus(PayoutStatus.PAID); // Already paid
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(testPayout));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            payoutService.markAsPaid(1L, 2L);
        });
    }

    @Test
    void testMarkAsReceived_UpdatesStatusAndSendsEmail() {
        // Given
        Long payoutId = 1L;
        Long sellerId = 1L;

        testPayout.setStatus(PayoutStatus.PAID);
        testPayout.setSellerId(sellerId);

        when(payoutRepository.findById(payoutId)).thenReturn(Optional.of(testPayout));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Arrays.asList(testAdmin));
        doNothing().when(emailService).sendPayoutReceivedEmail(any(), any(), any());

        // When
        payoutService.markAsReceived(payoutId, sellerId);

        // Then
        assertEquals(PayoutStatus.RECEIVED, testPayout.getStatus());
        assertNotNull(testPayout.getReceivedAt());
        verify(payoutRepository).save(testPayout);
        verify(emailService).sendPayoutReceivedEmail(eq("admin@example.com"), eq("testseller"), eq(new BigDecimal("900000")));
    }

    @Test
    void testMarkAsReceived_PayoutNotInPaidStatus_ThrowsException() {
        // Given
        testPayout.setStatus(PayoutStatus.NEEDS_PAYMENT); // Not paid yet
        testPayout.setSellerId(1L);
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(testPayout));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            payoutService.markAsReceived(1L, 1L);
        });
    }

    @Test
    void testMarkAsReceived_WrongSeller_ThrowsException() {
        // Given
        testPayout.setStatus(PayoutStatus.PAID);
        testPayout.setSellerId(1L); // Belongs to seller 1
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(testPayout));

        // When & Then - seller 2 tries to confirm
        assertThrows(IllegalStateException.class, () -> {
            payoutService.markAsReceived(1L, 2L);
        });
    }

    @Test
    void testGetPendingPayoutsForSeller() {
        // Given
        Long sellerId = 1L;
        List<Payout> paidPayouts = Arrays.asList(testPayout);
        testPayout.setStatus(PayoutStatus.PAID);

        when(payoutRepository.findBySellerIdAndStatus(sellerId, PayoutStatus.PAID))
            .thenReturn(paidPayouts);

        // When
        List<Payout> result = payoutService.getPendingPayoutsForSeller(sellerId);

        // Then
        assertEquals(1, result.size());
        assertEquals(PayoutStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void testGetSellerPayoutSummary() {
        // Given
        Long sellerId = 1L;
        Payout payout1 = new Payout();
        payout1.setId(1L);
        payout1.setSellerId(sellerId);
        payout1.setAmount(new BigDecimal("300000"));
        payout1.setStatus(PayoutStatus.PAID);

        Payout payout2 = new Payout();
        payout2.setId(2L);
        payout2.setSellerId(sellerId);
        payout2.setAmount(new BigDecimal("150000"));
        payout2.setStatus(PayoutStatus.PAID);

        when(payoutRepository.findBySellerIdAndStatus(sellerId, PayoutStatus.PAID))
            .thenReturn(Arrays.asList(payout1, payout2));

        // When
        var summary = payoutService.getSellerPayoutSummary(sellerId);

        // Then
        assertEquals(new BigDecimal("450000"), summary.totalPendingAmount());
        assertEquals(2, summary.pendingCount());
    }

    @Test
    void testGetPayoutsByStatus_WithSellerInfo() {
        // Given
        Payout payout1 = new Payout();
        payout1.setId(1L);
        payout1.setSellerId(1L);
        payout1.setAmount(new BigDecimal("900000"));
        payout1.setStatus(PayoutStatus.NEEDS_PAYMENT);

        when(payoutRepository.findByStatusOrderByCreatedAtDesc(PayoutStatus.NEEDS_PAYMENT))
            .thenReturn(Arrays.asList(payout1));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testSeller));

        // When
        List<AdminPayoutDto> result = payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);

        // Then
        assertEquals(1, result.size());
        assertEquals("testseller", result.get(0).sellerUsername());
        assertEquals("seller@example.com", result.get(0).sellerEmail());
    }

    @Test
    void testGetPayoutsByStatus_WithMissingSeller() {
        // Given
        Payout payout1 = new Payout();
        payout1.setId(1L);
        payout1.setSellerId(999L); // Non-existent seller
        payout1.setAmount(new BigDecimal("900000"));
        payout1.setStatus(PayoutStatus.NEEDS_PAYMENT);

        when(payoutRepository.findByStatusOrderByCreatedAtDesc(PayoutStatus.NEEDS_PAYMENT))
            .thenReturn(Arrays.asList(payout1));
        when(userRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // When
        List<AdminPayoutDto> result = payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);

        // Then
        assertEquals(1, result.size());
        assertEquals("Unknown Seller", result.get(0).sellerUsername());
        assertEquals("N/A", result.get(0).sellerEmail());
    }

    @Test
    void testGetPayoutsByStatus_EmptyList() {
        // Given
        when(payoutRepository.findByStatusOrderByCreatedAtDesc(any()))
            .thenReturn(Collections.emptyList());

        // When
        List<AdminPayoutDto> result = payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);

        // Then
        assertTrue(result.isEmpty());
    }
}
