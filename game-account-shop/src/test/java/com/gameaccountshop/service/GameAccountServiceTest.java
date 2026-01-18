package com.gameaccountshop.service;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAccountServiceTest {

    @Mock
    private GameAccountRepository gameAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameAccountService gameAccountService;

    private GameAccountDto testDto;
    private GameAccount testEntity;
    private User testUser;

    @BeforeEach
    void setUp() {
        testDto = new GameAccountDto();
        testDto.setAccountRank("Gold III");
        testDto.setPrice(500000L);
        testDto.setDescription("Tài khoản Gold 30 tướng");

        testEntity = new GameAccount();
        testEntity.setId(1L);
        testEntity.setGameName("Liên Minh Huyền Thoại");
        testEntity.setAccountRank("Gold III");
        testEntity.setPrice(500000L);
        testEntity.setDescription("Tài khoản Gold 30 tướng");
        testEntity.setSellerId(1L);
        testEntity.setStatus(ListingStatus.PENDING);
        testEntity.setCreatedAt(LocalDateTime.now());

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("seller1");
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
        // Use Answer to return the saved entity with the sellerId set
        when(gameAccountRepository.save(any(GameAccount.class))).thenAnswer(invocation -> {
            GameAccount saved = invocation.getArgument(0);
            return saved; // Return the same entity with sellerId already set
        });

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

    // ========================================================================
    // Story 2.2: Browse Listings with Search/Filter - Additional Tests
    // ========================================================================

    @Test
    void findApprovedListingsWithParams_NoFilters_ReturnsAllApproved() {
        // Given
        List<GameAccount> approvedAccounts = Arrays.asList(testEntity);
        when(gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED))
            .thenReturn(approvedAccounts);
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("seller1", result.get(0).getSellerUsername());
        verify(gameAccountRepository, times(1)).findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        verify(userRepository, times(1)).findAllById(any());
    }

    @Test
    void findApprovedListingsWithParams_WithSearchOnly_CallsSearchRepository() {
        // Given
        String searchTerm = "Liên Minh";
        when(gameAccountRepository.findByGameNameContainingAndStatus(eq(searchTerm), eq(ListingStatus.APPROVED)))
            .thenReturn(Arrays.asList(testEntity));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(searchTerm, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameAccountRepository, times(1)).findByGameNameContainingAndStatus(eq(searchTerm), eq(ListingStatus.APPROVED));
        verify(gameAccountRepository, never()).findByStatusAndAccountRank(any(), any());
    }

    @Test
    void findApprovedListingsWithParams_WithRankOnly_CallsRankRepository() {
        // Given
        String rank = "Gold";
        when(gameAccountRepository.findByStatusAndAccountRank(eq(rank), eq(ListingStatus.APPROVED)))
            .thenReturn(Arrays.asList(testEntity));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(null, rank);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameAccountRepository, times(1)).findByStatusAndAccountRank(eq(rank), eq(ListingStatus.APPROVED));
        verify(gameAccountRepository, never()).findByGameNameContainingAndStatus(any(), any());
    }

    @Test
    void findApprovedListingsWithParams_WithSearchAndRank_CombinesFilters() {
        // Given
        String searchTerm = "Liên Minh";
        String rank = "Gold";
        GameAccount goldEntity = new GameAccount();
        goldEntity.setId(2L);
        goldEntity.setGameName("Liên Minh Huyền Thoại");
        goldEntity.setAccountRank("Gold III");
        goldEntity.setSellerId(1L);
        goldEntity.setStatus(ListingStatus.APPROVED);

        GameAccount diamondEntity = new GameAccount();
        diamondEntity.setId(3L);
        diamondEntity.setGameName("Liên Minh Huyền Thoại");
        diamondEntity.setAccountRank("Diamond II");
        diamondEntity.setSellerId(1L);
        diamondEntity.setStatus(ListingStatus.APPROVED);

        List<GameAccount> byRank = Arrays.asList(goldEntity, diamondEntity);
        when(gameAccountRepository.findByStatusAndAccountRank(eq(rank), eq(ListingStatus.APPROVED)))
            .thenReturn(byRank);
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(searchTerm, rank);

        // Then
        assertNotNull(result);
        // Both match search term, so both should be returned
        assertEquals(2, result.size());
        verify(gameAccountRepository, times(1)).findByStatusAndAccountRank(eq(rank), eq(ListingStatus.APPROVED));
    }

    @Test
    void findApprovedListingsWithParams_EmptyResults_ReturnsEmptyList() {
        // Given
        when(gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED))
            .thenReturn(List.of());

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void findApprovedListingsWithParams_BlankSearchString_TreatedAsNoFilter() {
        // Given
        when(gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED))
            .thenReturn(Arrays.asList(testEntity));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings("   ", null);

        // Then
        assertNotNull(result);
        verify(gameAccountRepository, times(1)).findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    @Test
    void findApprovedListingsWithParams_CaseInsensitiveSearch_WorksCorrectly() {
        // Given - lowercase search term
        String searchTermLower = "liên minh"; // lowercase
        when(gameAccountRepository.findByGameNameContainingAndStatus(eq(searchTermLower), eq(ListingStatus.APPROVED)))
            .thenReturn(Arrays.asList(testEntity));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(testUser));

        // When
        List<ListingDisplayDto> result = gameAccountService.findApprovedListings(searchTermLower, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameAccountRepository, times(1)).findByGameNameContainingAndStatus(eq(searchTermLower), eq(ListingStatus.APPROVED));
    }
}
