package com.gameaccountshop.service;

import com.gameaccountshop.dto.AdminListingDto;
import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.dto.ListingDetailDto;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    // ========================================================================
    // Story 2.3: Listing Details Page - getListingDetail Tests
    // ========================================================================

    @Test
    void getListingDetail_ExistingApprovedListing_ReturnsDetailDto() {
        // Given
        Long listingId = 1L;
        ListingDetailDto expectedDto = new ListingDetailDto(
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
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.of(expectedDto));

        // When
        ListingDetailDto result = gameAccountService.getListingDetail(listingId);

        // Then
        assertNotNull(result);
        assertEquals(listingId, result.getId());
        assertEquals("Gold III", result.getAccountRank());
        assertEquals(500000L, result.getPrice());
        assertEquals("seller1", result.getSellerUsername());
        assertEquals(ListingStatus.APPROVED, result.getStatus());
        assertFalse(result.isSold());

        verify(gameAccountRepository, times(1)).findDetailById(listingId);
    }

    @Test
    void getListingDetail_ExistingSoldListing_ReturnsDetailDto() {
        // Given
        Long listingId = 2L;
        ListingDetailDto expectedDto = new ListingDetailDto(
            listingId,
            "Liên Minh Huyền Thoại",
            "Diamond II",
            1000000L,
            "Tài khoản Diamond",
            ListingStatus.SOLD,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now(),
            1L,
            "seller1",
            "seller@example.com"
        );
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.of(expectedDto));

        // When
        ListingDetailDto result = gameAccountService.getListingDetail(listingId);

        // Then
        assertNotNull(result);
        assertEquals(ListingStatus.SOLD, result.getStatus());
        assertTrue(result.isSold());
        assertNotNull(result.getSoldAt());

        verify(gameAccountRepository, times(1)).findDetailById(listingId);
    }

    @Test
    void getListingDetail_NonExistentListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 999L;
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> gameAccountService.getListingDetail(listingId)
        );

        assertEquals("Không tìm thấy tài khoản này", exception.getMessage());
        verify(gameAccountRepository, times(1)).findDetailById(listingId);
    }

    @Test
    void getListingDetail_PendingListing_ThrowsIllegalArgumentException() {
        // Given - PENDING listings should NOT be accessible (repository returns empty)
        Long listingId = 3L;
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> gameAccountService.getListingDetail(listingId)
        );

        assertEquals("Không tìm thấy tài khoản này", exception.getMessage());
        verify(gameAccountRepository, times(1)).findDetailById(listingId);
    }

    @Test
    void getListingDetail_RejectedListing_ThrowsIllegalArgumentException() {
        // Given - REJECTED listings should NOT be accessible (repository returns empty)
        Long listingId = 4L;
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> gameAccountService.getListingDetail(listingId)
        );

        assertEquals("Không tìm thấy tài khoản này", exception.getMessage());
        verify(gameAccountRepository, times(1)).findDetailById(listingId);
    }

    @Test
    void getListingDetail_AllFieldsPopulatedCorrectly() {
        // Given
        Long listingId = 5L;
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 10, 30);
        LocalDateTime soldAt = LocalDateTime.of(2026, 1, 20, 15, 45);
        ListingDetailDto expectedDto = new ListingDetailDto(
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
        when(gameAccountRepository.findDetailById(listingId)).thenReturn(Optional.of(expectedDto));

        // When
        ListingDetailDto result = gameAccountService.getListingDetail(listingId);

        // Then - Verify all fields
        assertEquals(listingId, result.getId());
        assertEquals("Liên Minh Huyền Thoại", result.getGameName());
        assertEquals("Master", result.getAccountRank());
        assertEquals(2000000L, result.getPrice());
        assertEquals("Full tướng full skin", result.getDescription());
        assertEquals(ListingStatus.SOLD, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(soldAt, result.getSoldAt());
        assertEquals(2L, result.getSellerId());
        assertEquals("proSeller", result.getSellerUsername());
        assertEquals("pro@example.com", result.getSellerEmail());
        assertTrue(result.isSold());
    }

    // ========================================================================
    // Story 2.4: Admin Approve/Reject Listings - Service Methods
    // ========================================================================

    @Test
    void findPendingListings_ExistingPendingListings_ReturnsListOfDtosOrderedByCreatedAtAsc() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("seller1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("seller2");

        GameAccount pending1 = new GameAccount();
        pending1.setId(1L);
        pending1.setGameName("Liên Minh Huyền Thoại");
        pending1.setAccountRank("Gold III");
        pending1.setPrice(500000L);
        pending1.setSellerId(1L);
        pending1.setStatus(ListingStatus.PENDING);
        pending1.setCreatedAt(LocalDateTime.of(2026, 1, 18, 10, 0)); // Later

        GameAccount pending2 = new GameAccount();
        pending2.setId(2L);
        pending2.setGameName("Liên Minh Huyền Thoại");
        pending2.setAccountRank("Diamond II");
        pending2.setPrice(1000000L);
        pending2.setSellerId(2L);
        pending2.setStatus(ListingStatus.PENDING);
        pending2.setCreatedAt(LocalDateTime.of(2026, 1, 18, 9, 0)); // Earlier - should come first (FIFO)

        when(gameAccountRepository.findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING))
            .thenReturn(Arrays.asList(pending2, pending1));
        when(userRepository.findAllById(Arrays.asList(2L, 1L))).thenReturn(Arrays.asList(user2, user1));

        // When
        List<AdminListingDto> result = gameAccountService.findPendingListings();

        // Then - Should be ordered by created_at ASC (oldest first - FIFO)
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id()); // Earlier listing comes first
        assertEquals(1L, result.get(1).id()); // Later listing comes second
        assertEquals("seller2", result.get(0).sellerUsername()); // Username for sellerId=2
        assertEquals("seller1", result.get(1).sellerUsername()); // Username for sellerId=1
        verify(gameAccountRepository, times(1)).findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING);
        verify(userRepository, times(1)).findAllById(any());
    }

    @Test
    void findPendingListings_NoPendingListings_ReturnsEmptyList() {
        // Given
        when(gameAccountRepository.findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING))
            .thenReturn(Collections.emptyList());

        // When
        List<AdminListingDto> result = gameAccountService.findPendingListings();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void approveListing_ExistingPendingListing_UpdatesStatusToApproved() {
        // Given
        Long listingId = 1L;
        GameAccount pendingListing = new GameAccount();
        pendingListing.setId(listingId);
        pendingListing.setAccountRank("Gold III");
        pendingListing.setPrice(500000L);
        pendingListing.setSellerId(1L);
        pendingListing.setStatus(ListingStatus.PENDING);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(pendingListing));
        when(gameAccountRepository.save(any(GameAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gameAccountService.approveListing(listingId);

        // Then
        assertEquals(ListingStatus.APPROVED, pendingListing.getStatus());
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, times(1)).save(pendingListing);
    }

    @Test
    void approveListing_NonExistentListing_ThrowsResourceNotFoundException() {
        // Given
        Long listingId = 999L;
        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.gameaccountshop.exception.ResourceNotFoundException.class, () -> {
            gameAccountService.approveListing(listingId);
        });
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void approveListing_AlreadyApprovedListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 1L;
        GameAccount approvedListing = new GameAccount();
        approvedListing.setId(listingId);
        approvedListing.setStatus(ListingStatus.APPROVED);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameAccountService.approveListing(listingId);
        });
        assertEquals(ListingStatus.APPROVED, approvedListing.getStatus()); // Status should remain unchanged
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void rejectListing_ExistingPendingListing_UpdatesStatusToRejectedWithReason() {
        // Given
        Long listingId = 1L;
        String reason = "Thông tin không chính xác";
        GameAccount pendingListing = new GameAccount();
        pendingListing.setId(listingId);
        pendingListing.setAccountRank("Gold III");
        pendingListing.setPrice(500000L);
        pendingListing.setSellerId(1L);
        pendingListing.setStatus(ListingStatus.PENDING);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(pendingListing));
        when(gameAccountRepository.save(any(GameAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gameAccountService.rejectListing(listingId, reason);

        // Then
        assertEquals(ListingStatus.REJECTED, pendingListing.getStatus());
        assertEquals(reason, pendingListing.getRejectionReason());
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, times(1)).save(pendingListing);
    }

    @Test
    void rejectListing_NonExistentListing_ThrowsResourceNotFoundException() {
        // Given
        Long listingId = 999L;
        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.gameaccountshop.exception.ResourceNotFoundException.class, () -> {
            gameAccountService.rejectListing(listingId, "Test reason");
        });
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void rejectListing_AlreadyApprovedListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 1L;
        GameAccount approvedListing = new GameAccount();
        approvedListing.setId(listingId);
        approvedListing.setStatus(ListingStatus.APPROVED);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameAccountService.rejectListing(listingId, "Test reason");
        });
        assertEquals(ListingStatus.APPROVED, approvedListing.getStatus()); // Status should remain unchanged
        assertNull(approvedListing.getRejectionReason()); // No reason should be stored
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    // ========================================================================
    // Story 2.5: Mark Listing as Sold - markAsSold Tests
    // ========================================================================

    @Test
    void markAsSold_ValidApprovedListing_UpdatesStatusToSold() {
        // Given
        Long listingId = 1L;
        GameAccount approvedListing = new GameAccount();
        approvedListing.setId(listingId);
        approvedListing.setGameName("Liên Minh Huyền Thoại");
        approvedListing.setAccountRank("Gold III");
        approvedListing.setPrice(500000L);
        approvedListing.setSellerId(1L);
        approvedListing.setStatus(ListingStatus.APPROVED);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
        when(gameAccountRepository.save(any(GameAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gameAccountService.markAsSold(listingId);

        // Then
        assertEquals(ListingStatus.SOLD, approvedListing.getStatus());
        assertNotNull(approvedListing.getSoldAt());
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, times(1)).save(approvedListing);
    }

    @Test
    void markAsSold_NonExistentListing_ThrowsResourceNotFoundException() {
        // Given
        Long listingId = 999L;
        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.gameaccountshop.exception.ResourceNotFoundException.class, () -> {
            gameAccountService.markAsSold(listingId);
        });
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void markAsSold_PendingListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 1L;
        GameAccount pendingListing = new GameAccount();
        pendingListing.setId(listingId);
        pendingListing.setStatus(ListingStatus.PENDING);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(pendingListing));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameAccountService.markAsSold(listingId);
        });
        assertEquals(ListingStatus.PENDING, pendingListing.getStatus()); // Status unchanged
        assertNull(pendingListing.getSoldAt());
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void markAsSold_RejectedListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 1L;
        GameAccount rejectedListing = new GameAccount();
        rejectedListing.setId(listingId);
        rejectedListing.setStatus(ListingStatus.REJECTED);

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(rejectedListing));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameAccountService.markAsSold(listingId);
        });
        assertEquals(ListingStatus.REJECTED, rejectedListing.getStatus()); // Status unchanged
        assertNull(rejectedListing.getSoldAt());
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }

    @Test
    void markAsSold_AlreadySoldListing_ThrowsIllegalArgumentException() {
        // Given
        Long listingId = 1L;
        GameAccount soldListing = new GameAccount();
        soldListing.setId(listingId);
        soldListing.setStatus(ListingStatus.SOLD);
        soldListing.setSoldAt(LocalDateTime.now().minusDays(1));

        when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(soldListing));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameAccountService.markAsSold(listingId);
        });
        assertEquals(ListingStatus.SOLD, soldListing.getStatus()); // Status unchanged
        assertNotNull(soldListing.getSoldAt()); // sold_at unchanged
        verify(gameAccountRepository, times(1)).findById(listingId);
        verify(gameAccountRepository, never()).save(any(GameAccount.class));
    }
}
