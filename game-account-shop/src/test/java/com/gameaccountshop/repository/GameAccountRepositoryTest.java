package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GameAccountRepositoryTest {

    @Autowired
    private GameAccountRepository gameAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private GameAccount testAccount1;
    private GameAccount testAccount2;

    @BeforeEach
    void setUp() {
        // Create test data
        testAccount1 = new GameAccount();
        testAccount1.setAccountRank("Gold III");
        testAccount1.setPrice(500000L);
        testAccount1.setDescription("Test account 1");
        testAccount1.setSellerId(1L);
        testAccount1.setStatus(ListingStatus.PENDING);

        testAccount2 = new GameAccount();
        testAccount2.setAccountRank("Diamond II");
        testAccount2.setPrice(1000000L);
        testAccount2.setDescription("Test account 2");
        testAccount2.setSellerId(2L);
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
        List<GameAccount> result = gameAccountRepository.findBySellerId(1L);

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
        // Given
        GameAccount account3 = new GameAccount();
        account3.setAccountRank("Platinum I");
        account3.setPrice(2000000L);
        account3.setDescription("Latest account");
        account3.setSellerId(1L);
        account3.setStatus(ListingStatus.APPROVED);

        entityManager.persist(testAccount2); // First APPROVED
        entityManager.flush();

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persist(account3); // Second APPROVED (later)
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
}
