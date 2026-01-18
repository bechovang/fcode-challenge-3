package com.gameaccountshop.controller;

import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.service.GameAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final GameAccountService gameAccountService;

    public HomeController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Home page - browse approved listings with optional search and filter
     * Story 2.2: Browse Listings with Search/Filter
     * GET /?search=...&rank=...
     */
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rank,
            Model model) {

        List<ListingDisplayDto> listings = gameAccountService.findApprovedListings(search, rank);
        model.addAttribute("listings", listings);
        model.addAttribute("search", search);
        model.addAttribute("rank", rank);

        return "home";
    }
}
