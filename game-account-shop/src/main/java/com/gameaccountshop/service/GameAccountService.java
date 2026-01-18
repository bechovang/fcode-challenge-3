package com.gameaccountshop.service;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;

    public GameAccountService(GameAccountRepository gameAccountRepository) {
        this.gameAccountRepository = gameAccountRepository;
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

    public java.util.List<GameAccount> findBySellerId(Long sellerId) {
        return gameAccountRepository.findBySellerId(sellerId);
    }

    public java.util.List<GameAccount> findApprovedListings() {
        return gameAccountRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }
}
