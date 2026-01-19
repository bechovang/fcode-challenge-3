package com.gameaccountshop.service;

import com.gameaccountshop.dto.AdminListingDto;
import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.dto.ListingDetailDto;
import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.exception.ResourceNotFoundException;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;

    public GameAccountService(GameAccountRepository gameAccountRepository, UserRepository userRepository) {
        this.gameAccountRepository = gameAccountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameAccount createListing(GameAccountDto dto, Long sellerId) {
        log.info("Creating new listing: sellerId={}, rank={}, price={}", sellerId, dto.getAccountRank(), dto.getPrice());

        GameAccount gameAccount = new GameAccount();
        // gameName is auto-set to "Liên Minh Huyền Thoại" in @PrePersist
        gameAccount.setAccountRank(dto.getAccountRank());
        gameAccount.setPrice(dto.getPrice());
        gameAccount.setDescription(dto.getDescription());
        gameAccount.setSellerId(sellerId);
        gameAccount.setStatus(ListingStatus.PENDING);

        GameAccount saved = gameAccountRepository.save(gameAccount);
        log.info("Listing created successfully: id={}, sellerId={}", saved.getId(), sellerId);

        return saved;
    }

    public List<GameAccount> findBySellerId(Long sellerId) {
        return gameAccountRepository.findBySellerId(sellerId);
    }

    /**
     * Find all approved listings (no filters)
     * Story 2.2: Browse Listings with Search/Filter
     * @deprecated Use findApprovedListings(String, String) instead
     */
    @Deprecated
    public List<GameAccount> findApprovedListings() {
        log.debug("Finding all approved listings");
        return gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    /**
     * Find approved listings with optional search and/or rank filter
     * Story 2.2: Browse Listings with Search/Filter
     * Returns ListingDisplayDto with seller username
     *
     * @param search Optional search keyword (game name LIKE)
     * @param rank Optional account rank filter (exact match)
     * @return List of ListingDisplayDto with seller username matching criteria
     */
    public List<ListingDisplayDto> findApprovedListings(String search, String rank) {
        log.info("Finding approved listings: search={}, rank={}", search, rank);

        List<GameAccount> gameAccounts;

        // Both search and rank provided - filter by rank first, then search in-memory (repository handles ORDER BY)
        if (search != null && !search.isBlank() && rank != null && !rank.isBlank()) {
            log.debug("Applying both search and rank filter");
            List<GameAccount> byRank = gameAccountRepository.findByStatusAndAccountRank(rank, ListingStatus.APPROVED);
            gameAccounts = byRank.stream()
                    .filter(g -> g.getGameName().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }
        // Only search provided
        else if (search != null && !search.isBlank()) {
            log.debug("Applying search filter only");
            gameAccounts = gameAccountRepository.findByGameNameContainingAndStatus(search, ListingStatus.APPROVED);
        }
        // Only rank provided
        else if (rank != null && !rank.isBlank()) {
            log.debug("Applying rank filter only");
            gameAccounts = gameAccountRepository.findByStatusAndAccountRank(rank, ListingStatus.APPROVED);
        }
        // No filters - return all approved
        else {
            log.debug("No filters applied, returning all approved listings");
            gameAccounts = gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        }

        // Build ListingDisplayDto with seller username lookup
        return buildListingDisplayDtos(gameAccounts);
    }

    /**
     * Build ListingDisplayDto list from GameAccount entities with seller username lookup
     * Story 2.2: Helper method to add seller usernames
     */
    private List<ListingDisplayDto> buildListingDisplayDtos(List<GameAccount> gameAccounts) {
        if (gameAccounts == null || gameAccounts.isEmpty()) {
            return List.of();
        }

        // Collect unique seller IDs
        List<Long> sellerIds = gameAccounts.stream()
                .map(GameAccount::getSellerId)
                .distinct()
                .toList();

        // Fetch all users in one query
        List<User> sellers = userRepository.findAllById(sellerIds);
        Map<Long, String> sellerUsernameMap = sellers.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // Build DTOs
        return gameAccounts.stream()
                .map(ga -> new ListingDisplayDto(
                        ga.getId(),
                        ga.getGameName(),
                        ga.getAccountRank(),
                        ga.getPrice(),
                        ga.getDescription(),
                        ga.getCreatedAt(),
                        sellerUsernameMap.getOrDefault(ga.getSellerId(), "Unknown")
                ))
                .toList();
    }

    /**
     * Get detailed information for a specific listing
     * Story 2.3: Listing Details Page
     * Only APPROVED or SOLD listings are accessible
     *
     * @param id Listing ID
     * @return ListingDetailDto with all information
     * @throws IllegalArgumentException if listing not found or not accessible
     */
    public ListingDetailDto getListingDetail(Long id) {
        log.info("Getting listing detail for id={}", id);

        return gameAccountRepository.findDetailById(id)
                .orElseThrow(() -> {
                    log.warn("Listing not found or not accessible (not APPROVED/SOLD): id={}", id);
                    return new IllegalArgumentException("Không tìm thấy tài khoản này");
                });
    }

    /**
     * Find all pending listings for admin review (oldest first - FIFO)
     * Story 2.4: Admin Approve/Reject Listings
     *
     * @return List of AdminListingDto with seller username ordered by created_at ASC
     */
    public List<AdminListingDto> findPendingListings() {
        log.info("Finding pending listings for admin review");
        List<GameAccount> pendingListings = gameAccountRepository.findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING);

        // Build DTOs with seller username lookup
        if (pendingListings.isEmpty()) {
            return List.of();
        }

        // Collect unique seller IDs
        List<Long> sellerIds = pendingListings.stream()
                .map(GameAccount::getSellerId)
                .distinct()
                .toList();

        // Fetch all users in one query
        List<User> sellers = userRepository.findAllById(sellerIds);
        Map<Long, String> sellerUsernameMap = sellers.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // Build AdminListingDto list
        return pendingListings.stream()
                .map(ga -> new AdminListingDto(
                        ga.getId(),
                        ga.getGameName(),
                        ga.getAccountRank(),
                        ga.getPrice(),
                        ga.getDescription(),
                        sellerUsernameMap.getOrDefault(ga.getSellerId(), "Unknown"),
                        ga.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Find all pending listings as entities (for internal use)
     * Story 2.4: Admin Approve/Reject Listings
     *
     * @return List of pending GameAccount entities ordered by created_at ASC
     */
    List<GameAccount> findPendingListingsEntities() {
        log.info("Finding pending listings entities for internal use");
        return gameAccountRepository.findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING);
    }

    /**
     * Approve a listing
     * Story 2.4: Admin Approve/Reject Listings
     *
     * @param id Listing ID
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalArgumentException if listing is not in PENDING status
     */
    @Transactional
    public void approveListing(Long id) {
        log.info("Admin approving listing: id={}", id);

        GameAccount listing = gameAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Listing not found for approval: id={}", id);
                    return new ResourceNotFoundException("Không tìm thấy tài khoản này");
                });

        if (listing.getStatus() != ListingStatus.PENDING) {
            log.warn("Cannot approve listing with status {}: id={}", listing.getStatus(), id);
            throw new IllegalArgumentException("Chỉ có thể duyệt tài khoản đang chờ (PENDING)");
        }

        listing.setStatus(ListingStatus.APPROVED);
        gameAccountRepository.save(listing);

        log.info("Admin approved listing: id={}", id);
    }

    /**
     * Reject a listing with reason
     * Story 2.4: Admin Approve/Reject Listings
     *
     * @param id Listing ID
     * @param reason Rejection reason
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalArgumentException if listing is not in PENDING status
     */
    @Transactional
    public void rejectListing(Long id, String reason) {
        log.info("Admin rejecting listing: id={}, reason={}", id, reason);

        GameAccount listing = gameAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Listing not found for rejection: id={}", id);
                    return new ResourceNotFoundException("Không tìm thấy tài khoản này");
                });

        if (listing.getStatus() != ListingStatus.PENDING) {
            log.warn("Cannot reject listing with status {}: id={}", listing.getStatus(), id);
            throw new IllegalArgumentException("Chỉ có thể từ chối tài khoản đang chờ (PENDING)");
        }

        listing.setStatus(ListingStatus.REJECTED);
        listing.setRejectionReason(reason);
        gameAccountRepository.save(listing);

        log.info("Admin rejected listing: id={}, reason={}", id, reason);
    }

    /**
     * Mark a listing as sold
     * Story 2.5: Mark Listing as Sold
     *
     * @param id Listing ID
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalArgumentException if listing is not APPROVED
     */
    @Transactional
    public void markAsSold(Long id) {
        log.info("Admin marking listing as sold: id={}", id);

        GameAccount listing = gameAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Listing not found for mark-as-sold: id={}", id);
                    return new ResourceNotFoundException("Không tìm thấy tài khoản này");
                });

        if (listing.getStatus() != ListingStatus.APPROVED) {
            log.warn("Cannot mark listing with status {} as SOLD: id={}", listing.getStatus(), id);
            throw new IllegalArgumentException("Chỉ có thể đánh dấu bán cho tài khoản đang đăng bán (APPROVED)");
        }

        listing.setStatus(ListingStatus.SOLD);
        listing.setSoldAt(LocalDateTime.now());
        gameAccountRepository.save(listing);

        log.info("Admin marked listing as sold: id={}, soldAt={}", id, listing.getSoldAt());
    }
}
