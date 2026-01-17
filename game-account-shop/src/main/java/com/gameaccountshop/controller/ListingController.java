package com.gameaccountshop.controller;

import com.gameaccountshop.dto.ListingCreateRequest;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.service.GameAccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for listing management
 * Handles listing creation and display
 */
@Controller
@RequestMapping("/listings")
public class ListingController {

    private static final Logger log = LoggerFactory.getLogger(ListingController.class);

    private final GameAccountService gameAccountService;

    public ListingController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Show create listing form
     * Requires authentication
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("Displaying create listing form");
        model.addAttribute("listing", new ListingCreateRequest());
        return "listings/create";
    }

    /**
     * Process listing creation form submission
     * @param request the listing create request with validation
     * @param result binding result for validation errors
     * @param redirectAttributes for flash attributes
     * @param authentication current authenticated user
     * @return redirect to home page on success, or back to form on validation error
     */
    @PostMapping("/create")
    public String createListing(@Valid @ModelAttribute("listing") ListingCreateRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        log.info("Processing listing creation request from user");

        if (result.hasErrors()) {
            log.warn("Validation errors in listing creation: {}", result.getFieldErrors());
            return "listings/create";
        }

        // Get seller ID from authenticated user (security - never from form)
        User user = (User) authentication.getPrincipal();
        Long sellerId = user.getId();

        // Create the listing through service layer
        gameAccountService.createListing(request, sellerId);

        // Add success message for display on home page
        redirectAttributes.addFlashAttribute("successMessage",
            "Đăng bán thành công! Chờ admin duyệt.");

        log.info("Listing created successfully for sellerId: {}", sellerId);

        // Redirect to home page (PRG pattern)
        return "redirect:/";
    }
}
