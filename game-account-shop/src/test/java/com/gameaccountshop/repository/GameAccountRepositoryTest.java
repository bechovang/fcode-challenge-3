package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
class GameAccountRepositoryTest {

    @Autowired
    private GameAccountRepository gameAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private GameAccount testAccount1;
    private GameAccount testAccount2;
    private Long seller1Id;  // Store for use in tests

    @BeforeEach
    void setUp() {
        // Clean up existing test data to avoid conflicts
        // Delete all GameAccounts first (due to foreign key)
        gameAccountRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test users first (required by foreign key constraint)
        // Use timestamp-based username to avoid duplicates
        String timestamp = String.valueOf(System.currentTimeMillis());

        User user1 = new User();
        user1.setUsername("seller1_" + timestamp);
        user1.setPassword("password1");
        user1.setEmail("seller1_" + timestamp + "@test.com");
        user1.setRole(Role.USER);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setUsername("seller2_" + timestamp);
        user2.setPassword("password2");
        user2.setEmail("seller2_" + timestamp + "@test.com");
        user2.setRole(Role.USER);
        entityManager.persist(user2);
        entityManager.flush();

        // Store seller ID for use in tests
        seller1Id = user1.getId();

        // Create test data
        testAccount1 = new GameAccount();
        testAccount1.setAccountRank("Gold III");
        testAccount1.setPrice(500000L);
        testAccount1.setDescription("Test account 1");
        testAccount1.setSellerId(user1.getId());
        testAccount1.setStatus(ListingStatus.PENDING);

        testAccount2 = new GameAccount();
        testAccount2.setAccountRank("Diamond II");
        testAccount2.setPrice(1000000L);
        testAccount2.setDescription("Test account 2");
        testAccount2.setSellerId(user2.getId());
        testAccount2.setStatus(ListingStatus.APPROVED);
    }

    @Test
    void save_ValidAccount_ReturnsSavedAccount() {
        // When
        GameAccount saved = gameAccountRepository.save(testAccount1);

        // Then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Gold III", saved.getAccountRank());
        assertEquals(500000L, saved.getPrice());
    }

    @Test
    void findBySellerId_ExistingSellerId_ReturnsAccounts() {
        // Given
        entityManager.persist(testAccount1);
        entityManager.persist(testAccount2);
        entityManager.flush();

        // When
        List<GameAccount> result = gameAccountRepository.findBySellerId(seller1Id);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gold III", result.get(0).getAccountRank());
    }

    @Test
    void findBySellerId_NonExistingSellerId_ReturnsEmptyList() {
        // Given
        entityManager.persist(testAccount1);
        entityManager.flush();

        // When
        List<GameAccount> result = gameAccountRepository.findBySellerId(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatus_ExistingStatus_ReturnsAccounts() {
        // Given
        entityManager.persist(testAccount1);
        entityManager.persist(testAccount2);
        entityManager.flush();

        // When
        List<GameAccount> result = gameAccountRepository.findByStatus(ListingStatus.APPROVED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ListingStatus.APPROVED, result.get(0).getStatus());
    }

    @Test
    void findByStatusOrderByCreatedAtDesc_ReturnsOrderedAccounts() {
        // Given - set explicit timestamps to ensure reliable ordering
        LocalDateTime earlierTime = LocalDateTime.of(2026, 1, 18, 10, 0);
        LocalDateTime laterTime = LocalDateTime.of(2026, 1, 19, 10, 0);

        testAccount2.setCreatedAt(earlierTime);
        entityManager.persist(testAccount2); // First APPROVED (earlier timestamp)
        entityManager.flush();

        GameAccount account3 = new GameAccount();
        account3.setAccountRank("Platinum I");
        account3.setPrice(2000000L);
        account3.setDescription("Latest account");
        account3.setSellerId(seller1Id);  // Use actual seller ID
        account3.setStatus(ListingStatus.APPROVED);
        account3.setCreatedAt(laterTime);  // Later timestamp

        entityManager.persist(account3); // Second APPROVED (later timestamp)
        entityManager.flush();

        // When
        List<GameAccount> result = gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Most recent should be first
        assertEquals("Platinum I", result.get(0).getAccountRank());
        assertEquals("Diamond II", result.get(1).getAccountRank());
    }

    @Test
    void findById_ExistingId_ReturnsAccount() {
        // Given
        GameAccount saved = entityManager.persist(testAccount1);
        entityManager.flush();

        // When
        var result = gameAccountRepository.findById(saved.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Gold III", result.get().getAccountRank());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // When
        var result = gameAccountRepository.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findApprovedListings_SearchBySellerName_ReturnsAccounts() {
        // Given
        entityManager.persist(testAccount1); // seller1 (Gold III, Pending - Wait, status is pending?)
        // Let's create an approved account for seller1
        GameAccount seller1Approved = new GameAccount();
        seller1Approved.setAccountRank("Platinum IV");
        seller1Approved.setPrice(600000L);
        seller1Approved.setDescription("Approved account by seller1");
        seller1Approved.setSellerId(seller1Id);
        seller1Approved.setStatus(ListingStatus.APPROVED);
        entityManager.persist(seller1Approved);

        // seller2 account is approved
        entityManager.persist(testAccount2);
        entityManager.flush();

        // When - search for "seller1"
        List<GameAccount> result = gameAccountRepository.findApprovedListings(
            "seller1", null, ListingStatus.APPROVED, org.springframework.data.domain.Sort.unsorted()
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Platinum IV", result.get(0).getAccountRank());
        assertEquals(seller1Id, result.get(0).getSellerId());
    }

    // ========== Story 3.3: My Listings - Filtering & Profit Tests ==========

    @Test
    void findBySellerIdOrderByCreatedAtDesc_ShouldReturnListingsInDescOrder() {
        // Given
        LocalDateTime earlierTime = LocalDateTime.of(2026, 1, 18, 10, 0);
        LocalDateTime laterTime = LocalDateTime.of(2026, 1, 19, 10, 0);

        testAccount1.setCreatedAt(earlierTime);
        entityManager.persist(testAccount1);

        GameAccount account3 = new GameAccount();
        account3.setAccountRank("Platinum I");
        account3.setPrice(2000000L);
        account3.setDescription("Latest account");
        account3.setSellerId(seller1Id);
        account3.setStatus(ListingStatus.PENDING);
        account3.setCreatedAt(laterTime);
        entityManager.persist(account3);
        entityManager.flush();

        // When
        List<GameAccount> result = gameAccountRepository.findBySellerIdOrderByCreatedAtDesc(seller1Id);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Most recent should be first
        assertEquals("Platinum I", result.get(0).getAccountRank());
        assertEquals("Gold III", result.get(1).getAccountRank());
    }

    @Test
    void findBySellerIdAndStatus_ShouldReturnOnlyMatchingListings() {
        // Given
        entityManager.persist(testAccount1); // PENDING
        entityManager.persist(testAccount2); // APPROVED, different seller

        GameAccount soldAccount = new GameAccount();
        soldAccount.setAccountRank("Silver I");
        soldAccount.setPrice(300000L);
        soldAccount.setDescription("Sold account");
        soldAccount.setSellerId(seller1Id);
        soldAccount.setStatus(ListingStatus.SOLD);
        entityManager.persist(soldAccount);
        entityManager.flush();

        // When
        List<GameAccount> pendingResult = gameAccountRepository.findBySellerIdAndStatus(seller1Id, ListingStatus.PENDING);
        List<GameAccount> soldResult = gameAccountRepository.findBySellerIdAndStatus(seller1Id, ListingStatus.SOLD);

        // Then
        assertEquals(1, pendingResult.size());
        assertEquals(ListingStatus.PENDING, pendingResult.get(0).getStatus());

        assertEquals(1, soldResult.size());
        assertEquals(ListingStatus.SOLD, soldResult.get(0).getStatus());
    }

    @Test
    void sumPriceBySellerIdAndStatus_ShouldReturnTotalPriceOfSoldListings() {
        // Given
        entityManager.persist(testAccount1); // PENDING, 500000

        GameAccount soldAccount1 = new GameAccount();
        soldAccount1.setAccountRank("Silver I");
        soldAccount1.setPrice(300000L);
        soldAccount1.setDescription("Sold account 1");
        soldAccount1.setSellerId(seller1Id);
        soldAccount1.setStatus(ListingStatus.SOLD);
        entityManager.persist(soldAccount1);

        GameAccount soldAccount2 = new GameAccount();
        soldAccount2.setAccountRank("Bronze II");
        soldAccount2.setPrice(700000L);
        soldAccount2.setDescription("Sold account 2");
        soldAccount2.setSellerId(seller1Id);
        soldAccount2.setStatus(ListingStatus.SOLD);
        entityManager.persist(soldAccount2);
        entityManager.flush();

        // When
        Long total = gameAccountRepository.sumPriceBySellerIdAndStatus(seller1Id, ListingStatus.SOLD);

        // Then
        // 300000 + 700000 = 1000000
        assertEquals(1000000L, total);
    }

    @Test
    void sumPriceBySellerIdAndStatus_WhenNoSoldListings_ShouldReturnZero() {
        // Given - seller1 has PENDING account, no SOLD
        entityManager.persist(testAccount1);

        // When
        Long total = gameAccountRepository.sumPriceBySellerIdAndStatus(seller1Id, ListingStatus.SOLD);

        // Then - COALESCE returns 0 for empty result
        assertEquals(0L, total);
    }

    @Test
    void sumPriceBySellerIdAndStatus_WithMultipleSoldListings_ShouldSumCorrectly() {
        // Given
        GameAccount sold1 = new GameAccount();
        sold1.setAccountRank("Gold I");
        sold1.setPrice(500000L);
        sold1.setSellerId(seller1Id);
        sold1.setStatus(ListingStatus.SOLD);
        entityManager.persist(sold1);

        GameAccount sold2 = new GameAccount();
        sold2.setAccountRank("Gold II");
        sold2.setPrice(1000000L);
        sold2.setSellerId(seller1Id);
        sold2.setStatus(ListingStatus.SOLD);
        entityManager.persist(sold2);

        GameAccount sold3 = new GameAccount();
        sold3.setAccountRank("Gold III");
        sold3.setPrice(200000L);
        sold3.setSellerId(seller1Id);
        sold3.setStatus(ListingStatus.SOLD);
        entityManager.persist(sold3);
        entityManager.flush();

        // When
        Long total = gameAccountRepository.sumPriceBySellerIdAndStatus(seller1Id, ListingStatus.SOLD);

        // Then
        // 500000 + 1000000 + 200000 = 1700000
        assertEquals(1700000L, total);
    }

}
