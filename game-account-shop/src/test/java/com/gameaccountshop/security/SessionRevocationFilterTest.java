package com.gameaccountshop.security;

import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SessionRevocationFilter
 * Tests global session revocation mechanism when users are deleted/banned
 */
@ExtendWith(MockitoExtension.class)
class SessionRevocationFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SessionRevocationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        lenient().when(request.getContextPath()).thenReturn("/game-account-shop");
    }

    @Test
    void doFilterInternal_UserExistsInDatabase_ContinuesFilterChain() throws Exception {
        // Given - Authenticated user that exists in database
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "testuser",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - Should continue the filter chain normally
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    void doFilterInternal_UserNotInDatabase_InvalidatesSessionAndRedirects() throws Exception {
        // Given - Authenticated user that NO LONGER exists in database
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "deleteduser",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.existsByUsername("deleteduser")).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - Should invalidate session and redirect to login
        verify(filterChain, never()).doFilter(any(), any());
        verify(response).sendRedirect("/game-account-shop/auth/login?expired");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_NoAuthentication_ContinuesFilterChain() throws Exception {
        // Given - No authentication (anonymous user)
        SecurityContextHolder.clearContext();

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - Should continue normally without checking database
        verify(filterChain, times(1)).doFilter(request, response);
        verify(userRepository, never()).existsByUsername(anyString());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void doFilterInternal_AnonymousUser_ContinuesFilterChain() throws Exception {
        // Given - Anonymous authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "anonymousUser",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - Should continue normally without checking database
        verify(filterChain, times(1)).doFilter(request, response);
        verify(userRepository, never()).existsByUsername(anyString());
        verify(response, never()).sendRedirect(anyString());
    }
}
