package com.gameaccountshop.controller;

import com.gameaccountshop.dto.ListingDisplayDto;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for HomeController - Story 2.2: Browse Listings with Search/Filter
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;

    private List<ListingDisplayDto> mockListings;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();

        // Create mock listings for testing
        mockListings = List.of(
            new ListingDisplayDto(
                1L,
                "Liên Minh Huyền Thoại",
                "Gold III",
                500000L,
                "Test description for Gold account",
                LocalDateTime.of(2026, 1, 18, 10, 0),
                "seller1"
            ),
            new ListingDisplayDto(
                2L,
                "Liên Minh Huyền Thoại",
                "Diamond II",
                2000000L,
                "Test description for Diamond account",
                LocalDateTime.of(2026, 1, 17, 15, 30),
                "seller2"
            )
        );
    }

    @Test
    void home_NoParameters_ReturnsAllApprovedListings() throws Exception {
        // Given
        when(gameAccountService.findApprovedListings(isNull(), isNull(), isNull()))
            .thenReturn(mockListings);

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("listings"));

        verify(gameAccountService, times(1)).findApprovedListings(isNull(), isNull(), isNull());
    }

    @Test
    void home_WithSearchParameter_PassesSearchToService() throws Exception {
        // Given
        String searchTerm = "Liên Minh";
        when(gameAccountService.findApprovedListings(eq(searchTerm), isNull(), isNull()))
            .thenReturn(mockListings);

        // When & Then
        mockMvc.perform(get("/").param("search", searchTerm))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("listings"))
            .andExpect(model().attribute("search", searchTerm));

        verify(gameAccountService, times(1)).findApprovedListings(eq(searchTerm), isNull(), isNull());
    }

    @Test
    void home_WithRankParameter_PassesRankToService() throws Exception {
        // Given
        String rank = "Gold";
        when(gameAccountService.findApprovedListings(isNull(), eq(rank), isNull()))
            .thenReturn(List.of(mockListings.get(0)));

        // When & Then
        mockMvc.perform(get("/").param("rank", rank))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("listings"))
            .andExpect(model().attribute("rank", rank));

        verify(gameAccountService, times(1)).findApprovedListings(isNull(), eq(rank), isNull());
    }

    @Test
    void home_WithSearchAndRankParameters_PassesBothToService() throws Exception {
        // Given
        String searchTerm = "Liên Minh";
        String rank = "Gold";
        when(gameAccountService.findApprovedListings(eq(searchTerm), eq(rank), isNull()))
            .thenReturn(List.of(mockListings.get(0)));

        // When & Then
        mockMvc.perform(get("/")
                .param("search", searchTerm)
                .param("rank", rank))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("listings"))
            .andExpect(model().attribute("search", searchTerm))
            .andExpect(model().attribute("rank", rank));

        verify(gameAccountService, times(1)).findApprovedListings(eq(searchTerm), eq(rank), isNull());
    }

    @Test
    void home_EmptyListings_ReturnsEmptyList() throws Exception {
        // Given
        when(gameAccountService.findApprovedListings(isNull(), isNull(), isNull()))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("listings"));

        verify(gameAccountService, times(1)).findApprovedListings(isNull(), isNull(), isNull());
    }

    @Test
    void home_CaseInsensitiveSearch_WorksCorrectly() throws Exception {
        // Given - test lowercase search
        String searchTermLower = "liên minh"; // lowercase
        when(gameAccountService.findApprovedListings(eq(searchTermLower), isNull(), isNull()))
            .thenReturn(mockListings);

        // When & Then
        mockMvc.perform(get("/").param("search", searchTermLower))
            .andExpect(status().isOk())
            .andExpect(model().attribute("search", searchTermLower));

        verify(gameAccountService, times(1)).findApprovedListings(eq(searchTermLower), isNull(), isNull());
    }
}
