package com.gameaccountshop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for the application
 * Provides centralized error handling with Vietnamese error messages
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle illegal argument exceptions (validation failures)
     * Returns user-friendly Vietnamese error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        log.warn("Validation error: {}", ex.getMessage());

        String errorMessage = ex.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Vui lòng điền đầy đủ thông tin";
        }

        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        return "redirect:/";
    }

    /**
     * Handle general exceptions
     * Logs error and returns generic error message
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error occurred", ex);

        redirectAttributes.addFlashAttribute("errorMessage",
            "Đã xảy ra lỗi. Vui lòng thử lại sau.");
        return "redirect:/";
    }
}
