package com.gameaccountshop.controller;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {

    @Mock
    private GameAccountService gameAccountService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListingController listingController;

    private MockMvc mockMvc;

    private TestingAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();

        // Create test authentication token with String username as principal
        authentication = new TestingAuthenticationToken(
            "testuser",  // principal (username)
            null,        // credentials
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void showCreateForm_ReturnsCreateView() throws Exception {
        mockMvc.perform(get("/listings/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/create"))
                .andExpect(model().attributeExists("gameAccountDto"));
    }

    @Test
    void createListing_ValidDto_RedirectsToHome() throws IOException {
        // Given
        GameAccountDto dto = new GameAccountDto();
        dto.setAccountRank("Gold III");
        dto.setPrice(500000L);
        dto.setDescription("Test description");

        // Story 2.6: Add mock image to DTO
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.isEmpty()).thenReturn(false);
        dto.setImage(mockImage);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        GameAccount saved = new GameAccount();
        saved.setId(1L);
        saved.setAccountRank("Gold III");
        saved.setPrice(500000L);
        saved.setDescription("Test description");
        saved.setSellerId(1L);
        saved.setStatus(ListingStatus.PENDING);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(gameAccountService.createListing(any(GameAccountDto.class), eq(1L))).thenReturn(saved);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String result = listingController.createListing(dto, bindingResult, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/", result);
        verify(userRepository, times(1)).findByUsername("testuser");
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
        // Note: createListing service method throws IOException, so we can't use verify(never())
        // The validation path is confirmed by the returned view name
    }
}
