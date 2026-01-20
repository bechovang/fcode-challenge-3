package com.gameaccountshop.security;

import com.gameaccountshop.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Global Session Revocation Filter
 *
 * This filter checks whether the authenticated user still exists in the database
 * on every request. If the user is not found (deleted/banned), it immediately
 * invalidates the HTTP session, clears the SecurityContext, and redirects to login.
 *
 * This handles scenarios such as:
 * - Admin deleting a user while they have an active session
 * - User accounts being banned/removed
 * - Database records being deleted while users are logged in
 *
 * The filter runs ONCE per request (Before Spring Security's authentication processing)
 * to ensure we check the database state before allowing access to protected resources.
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

        // Only check if user is authenticated (not anonymous)
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {

            String username = authentication.getName();

            // Check if user still exists in database
            boolean userExists = userRepository.existsByUsername(username);

            if (!userExists) {
                log.warn("User '{}' no longer exists in database. Invalidating session and redirecting to login.", username);

                // Clear the security context
                SecurityContextHolder.clearContext();

                // Invalidate the HTTP session
                if (request.getSession(false) != null) {
                    request.getSession().invalidate();
                }

                // Redirect to login page with session expired message
                String loginUrl = "/auth/login?expired";
                response.sendRedirect(request.getContextPath() + loginUrl);
                return; // Don't continue the filter chain
            }

            log.debug("User '{}' exists in database. Allowing request to proceed.", username);
        }

        // User exists or is anonymous - continue normally
        filterChain.doFilter(request, response);
    }
}
