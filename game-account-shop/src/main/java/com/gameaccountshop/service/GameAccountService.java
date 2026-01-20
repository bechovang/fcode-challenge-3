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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameAccountService {

    private static final Map<String, String> GAME_ALIASES = Map.of(
        "lol", "Liên Minh Huyền Thoại",
        "league of legends", "Liên Minh Huyền Thoại",
        "lien minh", "Liên Minh Huyền Thoại",
        "lmht", "Liên Minh Huyền Thoại"
    );

    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final EmailService emailService;

    public GameAccountService(GameAccountRepository gameAccountRepository,
                             UserRepository userRepository,
                             ImageUploadService imageUploadService,
                             EmailService emailService) {
        this.gameAccountRepository = gameAccountRepository;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
        this.emailService = emailService;
    }

    /**
     * Create a new listing with image upload
     * Story 2.1: Create Listing
     * Story 2.6: Image Upload for Listing
     */
    @Transactional
    public GameAccount createListing(GameAccountDto dto, Long sellerId) throws IOException {
        log.info("Creating new listing: sellerId={}, rank={}, price={}", sellerId, dto.getAccountRank(), dto.getPrice());

        // Story 2.6: Upload image first
        String imageUrl = null;
        MultipartFile imageFile = dto.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = imageUploadService.uploadImage(imageFile);
            log.info("Image uploaded successfully: {}", imageUrl);
        } else {
            throw new IllegalArgumentException("Vui lòng tải lên ảnh minh họa");
        }

        GameAccount gameAccount = new GameAccount();
        // gameName is auto-set to "Liên Minh Huyền Thoại" in @PrePersist
        gameAccount.setAccountRank(dto.getAccountRank());
        gameAccount.setPrice(dto.getPrice());
        gameAccount.setDescription(dto.getDescription());
        gameAccount.setAccountUsername(dto.getAccountUsername());
        gameAccount.setAccountPassword(dto.getAccountPassword());
        gameAccount.setImageUrl(imageUrl); // Story 2.6: Store image URL
        gameAccount.setSellerId(sellerId);
        gameAccount.setStatus(ListingStatus.PENDING);

        GameAccount saved = gameAccountRepository.save(gameAccount);
        log.info("Listing created successfully: id={}, sellerId={}, imageUrl={}", saved.getId(), sellerId, imageUrl);

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
     * @param search Optional search keyword (game name LIKE, description LIKE, rank LIKE)
     * @param rank Optional account rank filter (starts with)
     * @param sortParam Optional sort parameter (price_asc, price_desc, newest)
     * @return List of ListingDisplayDto with seller username matching criteria
     */
    public List<ListingDisplayDto> findApprovedListings(String search, String rank, String sortParam) {
        log.info("Finding approved listings: search={}, rank={}, sort={}", search, rank, sortParam);

        // 1. Handle Aliases
        String effectiveSearch = search;
        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase().trim();
            if (GAME_ALIASES.containsKey(lowerSearch)) {
                effectiveSearch = GAME_ALIASES.get(lowerSearch);
                log.debug("Mapped alias '{}' to '{}'", search, effectiveSearch);
            }
        }

        // 2. Handle Sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default (newest)
        if ("price_asc".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }

        // 3. Call Repository
        List<GameAccount> gameAccounts = gameAccountRepository.findApprovedListings(effectiveSearch, rank, ListingStatus.APPROVED, sort);

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

        Map<Long, String> sellerUsernameMap = buildSellerUsernameMap(gameAccounts);

        // Build DTOs
        return gameAccounts.stream()
                .map(ga -> new ListingDisplayDto(
                        ga.getId(),
                        ga.getGameName(),
                        ga.getAccountRank(),
                        ga.getPrice(),
                        ga.getDescription(),
                        ga.getImageUrl(),
                        ga.getCreatedAt(),
                        sellerUsernameMap.getOrDefault(ga.getSellerId(), "Unknown")
                ))
                .toList();
    }

    /**
     * Build a map of seller ID to username for the given game accounts
     * Story 2.2, 2.4: Helper method to avoid duplicate seller username lookup code
     *
     * @param gameAccounts List of game accounts to build seller map for
     * @return Map of seller ID to username
     */
    private Map<Long, String> buildSellerUsernameMap(List<GameAccount> gameAccounts) {
        if (gameAccounts == null || gameAccounts.isEmpty()) {
            return Map.of();
        }

        // Collect unique seller IDs
        List<Long> sellerIds = gameAccounts.stream()
                .map(GameAccount::getSellerId)
                .distinct()
                .toList();

        // Fetch all users in one query
        List<User> sellers = userRepository.findAllById(sellerIds);
        return sellers.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
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

        if (pendingListings.isEmpty()) {
            return List.of();
        }

        Map<Long, String> sellerUsernameMap = buildSellerUsernameMap(pendingListings);

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
     * Approve a listing
     * Story 2.4: Admin Approve/Reject Listings
     * Story 2.7: Listing Email Notifications
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

        // Story 2.7: Send approval email (async and non-blocking)
        try {
            User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            // Assuming standard URL structure. In a real app, this should come from config or request.
            String listingUrl = "http://localhost:8080/listings/" + id;

            emailService.sendListingApprovedEmail(
                seller.getEmail(),
                listing.getGameName(),
                listing.getAccountRank(),
                listing.getPrice(),
                listingUrl
            );
        } catch (Exception e) {
            log.error("Failed to initiate approval email sending for listing: {}", id, e);
        }
    }

    /**
     * Reject a listing with reason
     * Story 2.4: Admin Approve/Reject Listings
     * Story 2.7: Listing Email Notifications
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

        // Story 2.7: Send rejection email (async and non-blocking)
        try {
            User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            emailService.sendListingRejectedEmail(
                seller.getEmail(),
                listing.getGameName(),
                listing.getAccountRank(),
                listing.getPrice(),
                reason
            );
        } catch (Exception e) {
            log.error("Failed to initiate rejection email sending for listing: {}", id, e);
        }
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
