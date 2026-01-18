package com.gameaccountshop.service;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAccountServiceTest {

    @Mock
    private GameAccountRepository gameAccountRepository;

    @InjectMocks
    private GameAccountService gameAccountService;

    private GameAccountDto testDto;
    private GameAccount testEntity;

    @BeforeEach
    void setUp() {
        testDto = new GameAccountDto();
        testDto.setAccountRank("Gold III");
        testDto.setPrice(500000L);
        testDto.setDescription("Tài khoản Gold 30 tướng");

        testEntity = new GameAccount();
        testEntity.setId(1L);
        testEntity.setAccountRank("Gold III");
        testEntity.setPrice(500000L);
        testEntity.setDescription("Tài khoản Gold 30 tướng");
        testEntity.setSellerId(1L);
        testEntity.setStatus(ListingStatus.PENDING);
    }

    @Test
    void createListing_ValidDto_ReturnsSavedEntity() {
        // Given
        when(gameAccountRepository.save(any(GameAccount.class))).thenReturn(testEntity);

        // When
        GameAccount result = gameAccountService.createListing(testDto, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Gold III", result.getAccountRank());
        assertEquals(500000L, result.getPrice());
        assertEquals("Tài khoản Gold 30 tướng", result.getDescription());
        assertEquals(1L, result.getSellerId());
        assertEquals(ListingStatus.PENDING, result.getStatus());

        verify(gameAccountRepository, times(1)).save(any(GameAccount.class));
    }

    @Test
    void createListing_SetsPendingStatus() {
        // Given
        when(gameAccountRepository.save(any(GameAccount.class))).thenReturn(testEntity);

        // When
        GameAccount result = gameAccountService.createListing(testDto, 1L);

        // Then
        assertEquals(ListingStatus.PENDING, result.getStatus());
    }

    @Test
    void createListing_SetsSellerId() {
        // Given
        Long sellerId = 123L;
        when(gameAccountRepository.save(any(GameAccount.class))).thenReturn(testEntity);

        // When
        GameAccount result = gameAccountService.createListing(testDto, sellerId);

        // Then
        assertEquals(sellerId, result.getSellerId());
    }

    @Test
    void findBySellerId_ReturnsListOfAccounts() {
        // Given
        List<GameAccount> expected = Arrays.asList(testEntity);
        when(gameAccountRepository.findBySellerId(1L)).thenReturn(expected);

        // When
        List<GameAccount> result = gameAccountService.findBySellerId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gold III", result.get(0).getAccountRank());

        verify(gameAccountRepository, times(1)).findBySellerId(1L);
    }

    @Test
    void findApprovedListings_ReturnsApprovedAccounts() {
        // Given
        List<GameAccount> expected = Arrays.asList(testEntity);
        when(gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED))
            .thenReturn(expected);

        // When
        List<GameAccount> result = gameAccountService.findApprovedListings();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(gameAccountRepository, times(1)).findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }
}
