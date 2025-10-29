package com.swp.project.service.user;

import com.swp.project.entity.user.User;
import com.swp.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }


}
