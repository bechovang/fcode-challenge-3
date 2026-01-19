package com.gameaccountshop.controller;

import com.gameaccountshop.config.GlobalExceptionHandler;
import com.gameaccountshop.dto.ListingDetailDto;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.service.GameAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ListingsController (Story 2.3: Listing Details Page)
 * Tests the /listings/{id} endpoint for public listing detail view
 */
@ExtendWith(MockitoExtension.class)
class ListingsControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @InjectMocks
    private ListingsController listingsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ========================================================================
    // Story 2.3: Listing Detail Page Tests
    // ========================================================================

    @Test
    void listingDetail_ExistingApprovedListing_ReturnsDetailView() throws Exception {
        // Given
        Long listingId = 1L;
        ListingDetailDto detailDto = new ListingDetailDto(
            listingId,
            "Liên Minh Huyền Thoại",
            "Gold III",
            500000L,
            "Tài khoản Gold 30 tướng",
            ListingStatus.APPROVED,
            LocalDateTime.now(),
            null,
            1L,
            "seller1",
            "seller@example.com"
        );
        when(gameAccountService.getListingDetail(listingId)).thenReturn(detailDto);

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(view().name("listing-detail"))
                .andExpect(model().attributeExists("listing"))
                .andExpect(model().attribute("listing", detailDto));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_ExistingSoldListing_ReturnsDetailViewWithSoldStatus() throws Exception {
        // Given
        Long listingId = 2L;
        ListingDetailDto detailDto = new ListingDetailDto(
            listingId,
            "Liên Minh Huyền Thoại",
            "Diamond II",
            1000000L,
            "Tài khoản Diamond",
            ListingStatus.SOLD,
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now(),
            1L,
            "seller1",
            "seller@example.com"
        );
        when(gameAccountService.getListingDetail(listingId)).thenReturn(detailDto);

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(view().name("listing-detail"))
                .andExpect(model().attributeExists("listing"))
                .andExpect(model().attribute("listing", detailDto));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_NonExistentListing_RedirectsToHomeWithError() throws Exception {
        // Given
        Long listingId = 999L;
        when(gameAccountService.getListingDetail(listingId))
                .thenThrow(new IllegalArgumentException("Không tìm thấy tài khoản này"));

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Không tìm thấy tài khoản này"));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_PendingListing_RedirectsToHomeWithError() throws Exception {
        // Given - PENDING listings are not accessible (service throws exception)
        Long listingId = 3L;
        when(gameAccountService.getListingDetail(listingId))
                .thenThrow(new IllegalArgumentException("Không tìm thấy tài khoản này"));

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_RejectedListing_RedirectsToHomeWithError() throws Exception {
        // Given - REJECTED listings are not accessible
        Long listingId = 4L;
        when(gameAccountService.getListingDetail(listingId))
                .thenThrow(new IllegalArgumentException("Không tìm thấy tài khoản này"));

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_AllListingFieldsPassedToModel() throws Exception {
        // Given
        Long listingId = 5L;
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 10, 30);
        LocalDateTime soldAt = LocalDateTime.of(2026, 1, 20, 15, 45);

        ListingDetailDto detailDto = new ListingDetailDto(
            listingId,
            "Liên Minh Huyền Thoại",
            "Master",
            2000000L,
            "Full tướng full skin",
            ListingStatus.SOLD,
            createdAt,
            soldAt,
            2L,
            "proSeller",
            "pro@example.com"
        );
        when(gameAccountService.getListingDetail(listingId)).thenReturn(detailDto);

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listing", detailDto))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.equalTo(listingId))))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("gameName", org.hamcrest.Matchers.equalTo("Liên Minh Huyền Thoại"))))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("accountRank", org.hamcrest.Matchers.equalTo("Master"))))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("price", org.hamcrest.Matchers.equalTo(2000000L))))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("sellerUsername", org.hamcrest.Matchers.equalTo("proSeller"))))
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("status", org.hamcrest.Matchers.equalTo(ListingStatus.SOLD))));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_ServiceException_MessagePreservedInRedirect() throws Exception {
        // Given
        Long listingId = 999L;
        String errorMessage = "Listing not found";
        when(gameAccountService.getListingDetail(listingId))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // When & Then
        mockMvc.perform(get("/listings/{id}", listingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", errorMessage));

        verify(gameAccountService, times(1)).getListingDetail(listingId);
    }

    @Test
    void listingDetail_DifferentListingIds_EachHandledIndependently() throws Exception {
        // Given - multiple different listing IDs
        ListingDetailDto dto1 = new ListingDetailDto(1L, "Liên Minh Huyền Thoại", "Gold", 100000L,
                "desc", ListingStatus.APPROVED, LocalDateTime.now(), null, 1L, "seller", "seller@ex.com");
        ListingDetailDto dto2 = new ListingDetailDto(2L, "Liên Minh Huyền Thoại", "Diamond", 500000L,
                "desc2", ListingStatus.APPROVED, LocalDateTime.now(), null, 1L, "seller", "seller@ex.com");

        when(gameAccountService.getListingDetail(1L)).thenReturn(dto1);
        when(gameAccountService.getListingDetail(2L)).thenReturn(dto2);

        // When & Then - first listing
        mockMvc.perform(get("/listings/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.equalTo(1L))));

        // When & Then - second listing
        mockMvc.perform(get("/listings/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listing",
                        org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.equalTo(2L))));

        verify(gameAccountService, times(1)).getListingDetail(1L);
        verify(gameAccountService, times(1)).getListingDetail(2L);
    }
}
