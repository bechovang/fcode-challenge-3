package com.gameaccountshop.service;

import com.gameaccountshop.dto.GameAccountDTO;
import com.gameaccountshop.dto.ListingCreateRequest;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.AccountStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for GameAccountService - Listing Creation Business Logic
 *
 * Tests verify:
 * - Listing is created with valid data
 * - Status defaults to PENDING
 * - Seller ID is set correctly
 * - Entity is saved to database
 * - DTO is returned correctly
 */
@ExtendWith(MockitoExtension.class)
class GameAccountServiceTest {

    @Mock
    private GameAccountRepository repository;

    private GameAccountService gameAccountService;

    @BeforeEach
    void setUp() {
        gameAccountService = new GameAccountService(repository);
    }

    /**
     * AC #1-3: Given valid listing request, When createListing is called, Then listing is saved
     */
    @Test
    void testCreateListingWithValidData() {
        // Given: Valid listing request with all required fields
        ListingCreateRequest request = new ListingCreateRequest();
        request.setGameName("Liên Minh Huyền Thoại");
        request.setRank("Diamond");
        request.setPrice(new BigDecimal("500000"));
        request.setDescription("Tài khoản có nhiều skin quý giá");

        Long sellerId = 1L;

        // Mock the saved entity
        GameAccount savedEntity = new GameAccount();
        savedEntity.setId(1L);
        savedEntity.setSellerId(sellerId);
        savedEntity.setGameName(request.getGameName());
        savedEntity.setRank(request.getRank());
        savedEntity.setPrice(request.getPrice());
        savedEntity.setDescription(request.getDescription());
        savedEntity.setStatus(AccountStatus.PENDING);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(GameAccount.class))).thenReturn(savedEntity);

        // When: createListing is called
        GameAccountDTO result = gameAccountService.createListing(request, sellerId);

        // Then: Repository save is called
        ArgumentCaptor<GameAccount> captor = ArgumentCaptor.forClass(GameAccount.class);
        verify(repository).save(captor.capture());
        GameAccount toSave = captor.getValue();

        // Verify all fields are set correctly
        assertThat(toSave.getSellerId()).isEqualTo(sellerId);
        assertThat(toSave.getGameName()).isEqualTo("Liên Minh Huyền Thoại");
        assertThat(toSave.getRank()).isEqualTo("Diamond");
        assertThat(toSave.getPrice()).isEqualByComparingTo("500000");
        assertThat(toSave.getDescription()).isEqualTo("Tài khoản có nhiều skin quý giá");

        // Verify DTO is returned with correct data
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSellerId()).isEqualTo(sellerId);
        assertThat(result.getGameName()).isEqualTo("Liên Minh Huyền Thoại");
        assertThat(result.getRank()).isEqualTo("Diamond");
        assertThat(result.getPrice()).isEqualByComparingTo("500000");
        assertThat(result.getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    /**
     * AC #6: Verify default status is PENDING for all new listings
     */
    @Test
    void testCreateListingSetsPendingStatusByDefault() {
        // Given: Valid listing request
        ListingCreateRequest request = new ListingCreateRequest();
        request.setGameName("Valorant");
        request.setRank("Immortal");
        request.setPrice(new BigDecimal("1000000"));
        request.setDescription("Tài khoản Immortal");

        Long sellerId = 2L;

        GameAccount savedEntity = new GameAccount();
        savedEntity.setId(5L);
        savedEntity.setSellerId(sellerId);
        savedEntity.setGameName(request.getGameName());
        savedEntity.setRank(request.getRank());
        savedEntity.setPrice(request.getPrice());
        savedEntity.setDescription(request.getDescription());
        savedEntity.setStatus(AccountStatus.PENDING);

        when(repository.save(any(GameAccount.class))).thenReturn(savedEntity);

        // When: createListing is called
        gameAccountService.createListing(request, sellerId);

        // Then: Status is set to PENDING
        ArgumentCaptor<GameAccount> captor = ArgumentCaptor.forClass(GameAccount.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    /**
     * AC #7: Verify seller_id is set from authenticated user (not from form)
     */
    @Test
    void testCreateListingSetsSellerIdCorrectly() {
        // Given: Valid listing request
        ListingCreateRequest request = new ListingCreateRequest();
        request.setGameName("Genshin Impact");
        request.setRank("AR 60");
        request.setPrice(new BigDecimal("300000"));
        request.setDescription("Tài khoản AR 60 full 5*");

        Long sellerId = 99L; // Seller ID from authenticated user

        GameAccount savedEntity = new GameAccount();
        savedEntity.setId(10L);
        savedEntity.setSellerId(sellerId);
        savedEntity.setGameName(request.getGameName());
        savedEntity.setRank(request.getRank());
        savedEntity.setPrice(request.getPrice());
        savedEntity.setDescription(request.getDescription());
        savedEntity.setStatus(AccountStatus.PENDING);

        when(repository.save(any(GameAccount.class))).thenReturn(savedEntity);

        // When: createListing is called with sellerId from authentication
        gameAccountService.createListing(request, sellerId);

        // Then: sellerId is set correctly
        ArgumentCaptor<GameAccount> captor = ArgumentCaptor.forClass(GameAccount.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getSellerId()).isEqualTo(99L);
    }

    /**
     * Verify createdAt timestamp is generated by @PrePersist
     */
    @Test
    void testCreateListingGeneratesTimestamp() {
        // Given: Valid listing request
        ListingCreateRequest request = new ListingCreateRequest();
        request.setGameName("Liên Minh Huyền Thoại");
        request.setRank("Gold");
        request.setPrice(new BigDecimal("200000"));
        request.setDescription("Tài khoản Gold");

        Long sellerId = 1L;

        LocalDateTime now = LocalDateTime.now();
        GameAccount savedEntity = new GameAccount();
        savedEntity.setId(1L);
        savedEntity.setSellerId(sellerId);
        savedEntity.setGameName(request.getGameName());
        savedEntity.setRank(request.getRank());
        savedEntity.setPrice(request.getPrice());
        savedEntity.setDescription(request.getDescription());
        savedEntity.setStatus(AccountStatus.PENDING);
        savedEntity.setCreatedAt(now);

        when(repository.save(any(GameAccount.class))).thenReturn(savedEntity);

        // When: createListing is called
        GameAccountDTO result = gameAccountService.createListing(request, sellerId);

        // Then: DTO contains createdAt timestamp
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    /**
     * Verify price is stored with correct precision (BigDecimal)
     */
    @Test
    void testCreateListingHandlesBigDecimalPrice() {
        // Given: Listing with decimal price
        ListingCreateRequest request = new ListingCreateRequest();
        request.setGameName("Liên Minh Huyền Thoại");
        request.setRank("Challenger");
        request.setPrice(new BigDecimal("999999.99")); // Max precision
        request.setDescription("Tài khoản Challenger");

        Long sellerId = 1L;

        GameAccount savedEntity = new GameAccount();
        savedEntity.setId(1L);
        savedEntity.setSellerId(sellerId);
        savedEntity.setGameName(request.getGameName());
        savedEntity.setRank(request.getRank());
        savedEntity.setPrice(request.getPrice());
        savedEntity.setDescription(request.getDescription());
        savedEntity.setStatus(AccountStatus.PENDING);

        when(repository.save(any(GameAccount.class))).thenReturn(savedEntity);

        // When: createListing is called
        GameAccountDTO result = gameAccountService.createListing(request, sellerId);

        // Then: Price is preserved with correct precision
        assertThat(result.getPrice()).isEqualByComparingTo("999999.99");
    }
}
