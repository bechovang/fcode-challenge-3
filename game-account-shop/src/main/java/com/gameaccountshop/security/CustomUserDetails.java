package com.gameaccountshop.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails implementation that includes the user's database ID.
 * This allows Thymeleaf templates to access the user's ID via #authentication.principal.id
 */
public class CustomUserDetails extends User {

    private final Long id;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long id) {
        super(username, password, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
