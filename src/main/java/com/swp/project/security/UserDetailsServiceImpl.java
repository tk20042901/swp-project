package com.swp.project.security;

import com.swp.project.entity.user.User;
import com.swp.project.service.user.UserService;
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
    public UserDetails loadUserByUsername(String email) {
        User user = userService.findUserByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException("Incorrect email or password");
        }
        return user;
    }
}
