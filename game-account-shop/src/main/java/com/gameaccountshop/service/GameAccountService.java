package com.gameaccountshop.service;

import com.gameaccountshop.dto.GameAccountDTO;
import com.gameaccountshop.dto.ListingCreateRequest;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.AccountStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for GameAccount business logic
 * Handles listing creation and data operations
 */
@Service
@Transactional
public class GameAccountService {

    private static final Logger log = LoggerFactory.getLogger(GameAccountService.class);

    private final GameAccountRepository repository;

    public GameAccountService(GameAccountRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new game account listing
     * @param request the listing create request with all required fields
     * @param sellerId the ID of the user creating the listing
     * @return GameAccountDTO of the created listing
     * @throws IllegalArgumentException if database operation fails
     */
    public GameAccountDTO createListing(ListingCreateRequest request, Long sellerId) {
        log.info("Creating new listing for sellerId: {}, game: {}, rank: {}, price: {}",
            sellerId, request.getGameName(), request.getRank(), request.getPrice());

        try {
            GameAccount listing = new GameAccount();
            listing.setSellerId(sellerId);
            listing.setGameName(request.getGameName());
            listing.setRank(request.getRank());
            listing.setPrice(request.getPrice());
            listing.setDescription(request.getDescription());
            listing.setStatus(AccountStatus.PENDING);

            GameAccount saved = repository.save(listing);

            log.info("Listing created successfully with id: {}, status: {}", saved.getId(), saved.getStatus());

            return toDTO(saved);
        } catch (DataAccessException e) {
            log.error("Database error while creating listing for sellerId: {}", sellerId, e);
            throw new IllegalArgumentException("Lỗi cơ sở dữ liệu. Không thể tạo tin đăng. Vui lòng thử lại.");
        }
    }

    /**
     * Convert GameAccount entity to DTO
     * @param entity the entity to convert
     * @return GameAccountDTO with entity data
     */
    private GameAccountDTO toDTO(GameAccount entity) {
        return new GameAccountDTO(
            entity.getId(),
            entity.getSellerId(),
            entity.getGameName(),
            entity.getRank(),
            entity.getPrice(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }
}
