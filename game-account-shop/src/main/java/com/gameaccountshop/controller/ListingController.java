package com.gameaccountshop.controller;

import com.gameaccountshop.dto.GameAccountDto;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.service.GameAccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/listing")
public class ListingController {

    private final GameAccountService gameAccountService;
    private final UserRepository userRepository;

    public ListingController(GameAccountService gameAccountService, UserRepository userRepository) {
        this.gameAccountService = gameAccountService;
        this.userRepository = userRepository;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("gameAccountDto", new GameAccountDto());
        return "listing/create";
    }

    @PostMapping("/create")
    public String createListing(
            @Valid GameAccountDto gameAccountDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "listing/create";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        gameAccountService.createListing(gameAccountDto, user.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Đăng bán thành công! Chờ admin duyệt.");
        return "redirect:/";
    }
}
