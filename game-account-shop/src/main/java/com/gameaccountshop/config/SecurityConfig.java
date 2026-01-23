package com.gameaccountshop.config;

import com.gameaccountshop.security.SessionRevocationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SessionRevocationFilter sessionRevocationFilter;

    public SecurityConfig(SessionRevocationFilter sessionRevocationFilter) {
        this.sessionRevocationFilter = sessionRevocationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(sessionRevocationFilter, SecurityContextPersistenceFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/listings/create").hasRole("USER")
                .requestMatchers("/listings/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/wallet/**").authenticated()
                .requestMatchers("/purchase-pending").authenticated()
                .requestMatchers("/payment/success").authenticated()
                .requestMatchers("/payment/cancel").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth/login?expired")
            );

        return http.build();
    }
}
