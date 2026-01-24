package com.gameaccountshop.controller;

import com.gameaccountshop.dto.MyListingDto;
import com.gameaccountshop.dto.SellerPayoutSummaryDto;
import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.GameAccountService;
import com.gameaccountshop.service.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

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
class MyListingsControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @Mock
    private PayoutService payoutService;

    @InjectMocks
    private MyListingsController controller;

    @Mock
    private Model model;

    private List<MyListingDto> testListings;
    private List<Payout> emptyPendingPayouts;
    private SellerPayoutSummaryDto emptyPayoutSummary;
    private Long testUserId = 100L;

    @BeforeEach
    void setUp() {
        // Create test listings as DTOs
        MyListingDto listing1 = new MyListingDto(
            1L,
            "Liên Minh Huyền Thoại",
            "Gold III",
            500000L,
            "https://example.com/image1.jpg",
            "PENDING",
            LocalDateTime.now()
        );

        MyListingDto listing2 = new MyListingDto(
            2L,
            "Liên Minh Huyền Thoại",
            "Diamond II",
            1000000L,
            "https://example.com/image2.jpg",
            "APPROVED",
            LocalDateTime.now()
        );

        testListings = Arrays.asList(listing1, listing2);

        // Empty pending payouts (default state)
        emptyPendingPayouts = Collections.emptyList();
        emptyPayoutSummary = new SellerPayoutSummaryDto(BigDecimal.ZERO, 0, List.of());
    }

    @Test
    void showMyListings_AuthenticatedUser_ReturnsOwnListings() {
        // Given
        when(gameAccountService.findMyListings(eq(testUserId), isNull()))
            .thenReturn(testListings);
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(900000L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        // Create a mock authentication with CustomUserDetails principal
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
            "testuser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
            testUserId
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = controller.showMyListings(null, authentication, model);

        // Then
        assertEquals("my-listings", viewName);
        verify(gameAccountService, times(1)).findMyListings(eq(testUserId), isNull());
        verify(gameAccountService, times(1)).calculateProfit(testUserId);
        verify(payoutService).getPendingPayoutsForSeller(testUserId);
        verify(payoutService).getSellerPayoutSummary(testUserId);
        verify(model).addAttribute("listings", testListings);
        verify(model).addAttribute("profit", 900000L);
        verify(model).addAttribute("selectedStatus", null);
    }

    @Test
    void showMyListings_WithStatusFilter_ReturnsFilteredListings() {
        // Given
        when(gameAccountService.findMyListings(eq(testUserId), eq("APPROVED")))
            .thenReturn(Collections.singletonList(testListings.get(1)));
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(900000L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
            "testuser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
            testUserId
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = controller.showMyListings("APPROVED", authentication, model);

        // Then
        assertEquals("my-listings", viewName);
        verify(gameAccountService, times(1)).findMyListings(eq(testUserId), eq("APPROVED"));
        verify(model).addAttribute("selectedStatus", "APPROVED");
    }

    @Test
    void showMyListings_WithNoListings_ReturnsEmptyList() {
        // Given
        when(gameAccountService.findMyListings(eq(testUserId), any()))
            .thenReturn(Collections.emptyList());
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(0L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
            "testuser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
            testUserId
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = controller.showMyListings(null, authentication, model);

        // Then
        assertEquals("my-listings", viewName);
        verify(gameAccountService, times(1)).findMyListings(eq(testUserId), isNull());
        verify(model).addAttribute("listings", Collections.emptyList());
    }

    @Test
    void showMyListings_CalculatesCorrectProfit() {
        // Given - 2 sold listings: 500,000 + 1,000,000 = 1,500,000 total
        // Profit = 1,500,000 * 0.90 = 1,350,000
        when(gameAccountService.findMyListings(eq(testUserId), any()))
            .thenReturn(testListings);
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(1350000L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
            "testuser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
            testUserId
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = controller.showMyListings(null, authentication, model);

        // Then
        assertEquals("my-listings", viewName);
        verify(model).addAttribute("profit", 1350000L);
    }

    @Test
    void showMyListings_WithAllStatus_ShowsAllListings() {
        // Given
        when(gameAccountService.findMyListings(eq(testUserId), eq("All")))
            .thenReturn(testListings);
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(900000L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
            "testuser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
            testUserId
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = controller.showMyListings("All", authentication, model);

        // Then
        assertEquals("my-listings", viewName);
        verify(gameAccountService, times(1)).findMyListings(eq(testUserId), eq("All"));
    }

    @Test
    void showMyListings_NullPrincipal_ThrowsException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            controller.showMyListings(null, authentication, model);
        });
    }

    @Test
    void showMyListings_LongPrincipal_WorksForTests() {
        // Given - For unit tests that pass Long directly
        when(gameAccountService.findMyListings(eq(testUserId), any()))
            .thenReturn(testListings);
        when(gameAccountService.calculateProfit(testUserId)).thenReturn(900000L);
        when(payoutService.getPendingPayoutsForSeller(testUserId)).thenReturn(emptyPendingPayouts);
        when(payoutService.getSellerPayoutSummary(testUserId)).thenReturn(emptyPayoutSummary);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserId); // Long directly

        // When
        String viewName = controller.showMyListings(null, authentication, model);

        // Then
        assertEquals("my-listings", viewName);
    }
}
