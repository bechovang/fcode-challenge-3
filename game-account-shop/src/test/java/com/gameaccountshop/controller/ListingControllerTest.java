package com.gameaccountshop.controller;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.service.GameAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @InjectMocks
    private ListingController listingController;

    private MockMvc mockMvc;

    private TestingAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();

        // Create test authentication token
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        authentication = new TestingAuthenticationToken(
            testUser,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void showCreateForm_ReturnsCreateView() throws Exception {
        mockMvc.perform(get("/listing/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/create"))
                .andExpect(model().attributeExists("gameAccountDto"));
    }

    @Test
    void createListing_ValidDto_RedirectsToHome() {
        // Given
        GameAccountDto dto = new GameAccountDto();
        dto.setAccountRank("Gold III");
        dto.setPrice(500000L);
        dto.setDescription("Test description");

        User user = new User();
        user.setId(1L);

        GameAccount saved = new GameAccount();
        saved.setId(1L);
        saved.setAccountRank("Gold III");
        saved.setPrice(500000L);
        saved.setDescription("Test description");
        saved.setSellerId(1L);
        saved.setStatus(ListingStatus.PENDING);

        when(gameAccountService.createListing(any(GameAccountDto.class), eq(1L))).thenReturn(saved);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String result = listingController.createListing(dto, bindingResult, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/", result);
        verify(gameAccountService, times(1)).createListing(any(GameAccountDto.class), eq(1L));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    void createListing_HasValidationErrors_ReturnsCreateView() {
        // Given
        GameAccountDto dto = new GameAccountDto();
        // Invalid data - empty fields

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String result = listingController.createListing(dto, bindingResult, authentication, redirectAttributes);

        // Then
        assertEquals("listing/create", result);
        verify(gameAccountService, never()).createListing(any(), any());
    }
}
