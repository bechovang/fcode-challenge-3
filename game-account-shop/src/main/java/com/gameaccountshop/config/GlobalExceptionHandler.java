package com.gameaccountshop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for all controllers
 * Provides consistent error handling across the application
 * Per project-context.md requirement: "Use @ControllerAdvice for ALL exception handling"
 *
 * All handlers use RedirectAttributes to pass error messages during redirects
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle NumberFormatException for invalid path variables (e.g., /listings/abc)
     * Returns Vietnamese error message and redirects to home page
     */
    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(NumberFormatException ex, RedirectAttributes redirectAttributes) {
        log.warn("Invalid ID format - NumberFormatException: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản này");
        return "redirect:/";
    }

    /**
     * Handle IllegalArgumentException for business logic errors
     * Returns Vietnamese error message and redirects to home page
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        log.warn("Business logic error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Handle all other exceptions generically
     * Logs full stack trace for troubleshooting
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra. Vui lòng thử lại.");
        return "redirect:/";
    }
}
