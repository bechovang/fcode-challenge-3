package com.gameaccountshop.security;

import com.gameaccountshop.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Session Revocation Filter
 *
 * Security filter that checks if authenticated users still exist in the database.
 * If a user is deleted or banned while having an active session, this filter
 * will invalidate their session and redirect to login page.
 *
 * This prevents deleted/banned users from continuing to access the application
 * with their old session tokens.
 */
@Slf4j
@Component
public class SessionRevocationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionRevocationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Skip check for anonymous users (not logged in)
        if (authentication == null || !authentication.isAuthenticated() ||
            isAnonymousUser(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get username from authentication
        String username = authentication.getName();

        // Check if user still exists in database
        boolean userExists = userRepository.existsByUsername(username);

        if (!userExists) {
            log.warn("User '{}' no longer exists in database. Invalidating session.", username);

            // Clear the security context
            SecurityContextHolder.clearContext();

            // Invalidate session and redirect to login with expired flag
            response.sendRedirect(request.getContextPath() + "/auth/login?expired");
            return;
        }

        // User exists, continue normally
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the authentication is for an anonymous user
     */
    private boolean isAnonymousUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grant -> grant instanceof SimpleGrantedAuthority &&
                         grant.getAuthority().equals("ROLE_ANONYMOUS"));
    }
}
