package com.gameaccountshop.controller;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.dto.ListingDetailDto;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.service.GameAccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller for listing operations
 * Story 2.1: Create Listing
 * Story 2.3: Listing Details Page
 * Story 2.6: Image Upload for Listing
 */
@Slf4j
@Controller
@RequestMapping("/listings")
public class ListingController {

    private final GameAccountService gameAccountService;
    private final UserRepository userRepository;

    public ListingController(GameAccountService gameAccountService, UserRepository userRepository) {
        this.gameAccountService = gameAccountService;
        this.userRepository = userRepository;
    }

    /**
     * Show create listing form
     * Story 2.1: Create Listing
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("gameAccountDto", new GameAccountDto());
        return "listing/create";
    }

    /**
     * Create a new listing with image upload
     * Story 2.1: Create Listing
     * Story 2.6: Image Upload for Listing
     */
    @PostMapping("/create")
    public String createListing(
            @Valid GameAccountDto gameAccountDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.debug("Validation errors in create listing form");
            return "listing/create";
        }

        // Story 2.6: Validate image is uploaded
        if (gameAccountDto.getImage() == null || gameAccountDto.getImage().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng tải lên ảnh minh họa");
            return "redirect:/listings/create";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + username));

            gameAccountService.createListing(gameAccountDto, user.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Đăng bán thành công! Chờ admin duyệt.");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating listing: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/create";

        } catch (IOException e) {
            log.error("IO error creating listing", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải lên ảnh. Vui lòng thử lại.");
            return "redirect:/listings/create";

        } catch (Exception e) {
            log.error("Error creating listing", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo listing. Vui lòng thử lại.");
            return "redirect:/listings/create";
        }
    }
}
