package com.gameaccountshop.config;

import com.gameaccountshop.repository.UserRepository;
import com.gameaccountshop.security.SessionRevocationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    /**
     * Session Revocation Filter Bean
     * Checks if authenticated user exists in database on every request
     * Invalidates session if user was deleted/banned
     */
    @Bean
    public SessionRevocationFilter sessionRevocationFilter() {
        return new SessionRevocationFilter(userRepository);
    }

    /**
     * BCrypt Password Encoder with 10 rounds (NFR-011 requirement)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Security Filter Chain configuration
     * - Session revocation filter (runs BEFORE authentication)
     * - Form login with custom pages
     * - Session management with 30-minute timeout (NFR-012 requirement)
     * - Logout with session invalidation
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/home", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/listings/create").hasRole("USER") // Seller create listing (Story 2.1) - MUST come before /listings/**
                .requestMatchers("/listings/**").permitAll() // Public listing detail pages (Story 2.3)
                .requestMatchers("/admin/**").hasRole("ADMIN") // Admin only (Story 2.4)
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth/login?expired")
            )
            // Register session revocation filter BEFORE Spring Security's authentication filter
            // This ensures we check the database state BEFORE allowing access to protected resources
            .addFilterBefore(sessionRevocationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
