package com.gameaccountshop.controller;

import com.gameaccountshop.dto.UserRegistrationRequest;
import com.gameaccountshop.exception.BusinessException;
import com.gameaccountshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Show registration form
     * @param model Spring MVC model
     * @return registration template name
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRequest", new UserRegistrationRequest());
        return "auth/register";
    }

    /**
     * Process user registration
     * @param request UserRegistrationRequest with @Valid for Bean validation
     * @param result BindingResult for validation errors
     * @param model Spring MVC model
     * @param redirectAttributes RedirectAttributes for flash messages
     * @return redirect to home page on success, registration form on error
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRequest") UserRegistrationRequest request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(request);
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công!");
            return "redirect:/?success";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * Show login form
     * @return login template name
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
}
