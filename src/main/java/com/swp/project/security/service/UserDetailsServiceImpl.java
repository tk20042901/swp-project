package com.swp.project.security.service;

import com.swp.project.entity.User;
import com.swp.project.security.CustomUserDetails;
import com.swp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        if(user == null) {
            throw new UsernameNotFoundException("Incorrect username or password");
        }
        return new CustomUserDetails(user);
    }
}
