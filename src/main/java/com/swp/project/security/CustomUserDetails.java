package com.swp.project.security;

import com.swp.project.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CustomUserDetails(User user, Map<String, Object> attributes) implements OAuth2User, UserDetails {
    public CustomUserDetails(User user) {
        this(user, Collections.emptyMap());
    }

    // Custom methods
    public String getEmail() {
        return user.getEmail();
    }

    //UserDetails override
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    //OAuth2User override
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getName() {
        return user.getEmail();
    }

    //Authority override
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getName()));
    }
}