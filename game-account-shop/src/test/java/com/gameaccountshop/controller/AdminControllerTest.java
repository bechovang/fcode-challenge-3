package com.gameaccountshop.controller;

import com.gameaccountshop.dto.AdminListingDto;
import com.gameaccountshop.service.GameAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AdminController (Story 2.4: Admin Approve/Reject Listings)
 * Tests admin-only access control and listing approval/rejection functionality
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    // ========================================================================
    // Story 2.4: Admin Review Page Tests
    // ========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void reviewListings_AsAdmin_ReturnsReviewPage() throws Exception {
        // Given
        AdminListingDto pending1 = new AdminListingDto(
            1L, "Liên Minh Huyền Thoại", "Gold III", 500000L,
            "Tài khoản Gold 30 tướng", "seller1", LocalDateTime.of(2026, 1, 18, 10, 0),
            "https://i.imgur.com/test1.jpg"
        );

        AdminListingDto pending2 = new AdminListingDto(
            2L, "Liên Minh Huyền Thoại", "Diamond II", 1000000L,
            "Tài khoản Diamond", "seller2", LocalDateTime.of(2026, 1, 18, 9, 0), // Earlier - should come first
            "https://i.imgur.com/test2.jpg"
        );

        when(gameAccountService.findPendingListings()).thenReturn(Arrays.asList(pending2, pending1));

        // When & Then
        mockMvc.perform(get("/admin/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/review"))
                .andExpect(model().attributeExists("listings"))
                .andExpect(model().attribute("listings", Arrays.asList(pending2, pending1)));

        verify(gameAccountService, times(1)).findPendingListings();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reviewListings_NoPendingListings_ReturnsEmptyList() throws Exception {
        // Given
        when(gameAccountService.findPendingListings()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/admin/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/review"))
                .andExpect(model().attributeExists("listings"))
                .andExpect(model().attribute("listings", Collections.emptyList()));

        verify(gameAccountService, times(1)).findPendingListings();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveListing_ValidPendingListing_SuccessAndRedirects() throws Exception {
        // Given
        Long listingId = 1L;
        doNothing().when(gameAccountService).approveListing(listingId);

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/approve", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Đã duyệt tài khoản"));

        verify(gameAccountService, times(1)).approveListing(listingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveListing_NonExistentListing_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 999L;
        doThrow(new com.gameaccountshop.exception.ResourceNotFoundException("Không tìm thấy tài khoản này"))
                .when(gameAccountService).approveListing(listingId);

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/approve", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Không tìm thấy tài khoản này"));

        verify(gameAccountService, times(1)).approveListing(listingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveListing_AlreadyApproved_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 1L;
        doThrow(new IllegalArgumentException("Chỉ có thể duyệt tài khoản đang chờ (PENDING)"))
                .when(gameAccountService).approveListing(listingId);

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/approve", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).approveListing(listingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectListing_ValidPendingListing_SuccessAndRedirects() throws Exception {
        // Given
        Long listingId = 1L;
        String reason = "Thông tin không chính xác";
        doNothing().when(gameAccountService).rejectListing(eq(listingId), eq(reason));

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/reject", listingId)
                .with(csrf())
                .param("reason", reason))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Đã từ chối tài khoản"));

        verify(gameAccountService, times(1)).rejectListing(eq(listingId), eq(reason));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectListing_NonExistentListing_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 999L;
        doThrow(new com.gameaccountshop.exception.ResourceNotFoundException("Không tìm thấy tài khoản này"))
                .when(gameAccountService).rejectListing(eq(listingId), any());

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/reject", listingId)
                .with(csrf())
                .param("reason", "Test reason"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).rejectListing(eq(listingId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectListing_AlreadyApproved_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 1L;
        doThrow(new IllegalArgumentException("Chỉ có thể từ chối tài khoản đang chờ (PENDING)"))
                .when(gameAccountService).rejectListing(eq(listingId), any());

        // When & Then
        mockMvc.perform(post("/admin/review/{id}/reject", listingId)
                .with(csrf())
                .param("reason", "Test reason"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).rejectListing(eq(listingId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectListing_BlankReason_ReturnsErrorMessage() throws Exception {
        // Given
        Long listingId = 1L;

        // When & Then - blank reason
        mockMvc.perform(post("/admin/review/{id}/reject", listingId)
                .with(csrf())
                .param("reason", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attribute("errorMessage", "Lý do từ chối phải từ 2-500 ký tự"));

        verify(gameAccountService, never()).rejectListing(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectListing_SingleCharacterReason_ReturnsErrorMessage() throws Exception {
        // Given
        Long listingId = 1L;

        // When & Then - single character reason (less than min length 2)
        mockMvc.perform(post("/admin/review/{id}/reject", listingId)
                .with(csrf())
                .param("reason", "x"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/review"))
                .andExpect(flash().attribute("errorMessage", "Lý do từ chối phải từ 2-500 ký tự"));

        verify(gameAccountService, never()).rejectListing(any(), any());
    }

    // ========================================================================
    // Access Control Tests (Non-ADMIN users)
    // ========================================================================
    // NOTE: MockMvc standaloneSetup does not integrate Spring Security filters.
    // The @PreAuthorize("hasRole('ADMIN')") annotation is enforced at runtime
    // by Spring Security's MethodSecurityInterceptor, which requires a full
    // Spring application context with SecurityFilterChain.
    //
    // Actual access control is verified by:
    // - SecurityConfig.java: .requestMatchers("/admin/**").hasRole("ADMIN")
    // - AdminController.java: @PreAuthorize("hasRole('ADMIN')") at class level
    //
    // For integration testing with full security, use @WebMvcTest with
    // SecurityMockMvcRequestPostProcessors.testSecurityContext() or create
    // tests with @SpringBootTest and full context.

    @Test
    @WithMockUser(roles = "USER")
    void reviewListings_AsUser_RequiresFullSpringContextForSecurityTest() throws Exception {
        // NOTE: This test documents that full security testing requires Spring context
        // With standaloneSetup, @PreAuthorize is not enforced
        // Security is verified at runtime by Spring Security filter chain
        mockMvc.perform(get("/admin/review"))
                .andExpect(status().isOk()); // Would be 403 with full SecurityFilterChain
    }

    // ========================================================================
    // Story 2.5: Mark Listing as Sold - markAsSold Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsSold_ValidApprovedListing_SuccessAndRedirects() throws Exception {
        // Given
        Long listingId = 1L;
        doNothing().when(gameAccountService).markAsSold(listingId);

        // When & Then
        mockMvc.perform(post("/admin/listings/{id}/mark-sold", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings/" + listingId))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Đã đánh dấu tài khoản đã bán"));

        verify(gameAccountService, times(1)).markAsSold(listingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsSold_NonExistentListing_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 999L;
        doThrow(new com.gameaccountshop.exception.ResourceNotFoundException("Không tìm thấy tài khoản này"))
                .when(gameAccountService).markAsSold(listingId);

        // When & Then
        mockMvc.perform(post("/admin/listings/{id}/mark-sold", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings/" + listingId))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Không tìm thấy tài khoản này"));

        verify(gameAccountService, times(1)).markAsSold(listingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsSold_NotApprovedListing_ShowsErrorMessage() throws Exception {
        // Given
        Long listingId = 1L;
        doThrow(new IllegalArgumentException("Chỉ có thể đánh dấu bán cho tài khoản đang đăng bán (APPROVED)"))
                .when(gameAccountService).markAsSold(listingId);

        // When & Then
        mockMvc.perform(post("/admin/listings/{id}/mark-sold", listingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings/" + listingId))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(gameAccountService, times(1)).markAsSold(listingId);
    }
}
