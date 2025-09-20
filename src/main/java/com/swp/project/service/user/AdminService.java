package com.swp.project.service.user;

import com.swp.project.entity.user.Admin;
import com.swp.project.repository.user.AdminRepository;
import com.swp.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;
    @Transactional
    public void initAdmin() {
        if(!userRepository.existsByEmail(adminEmail)){
            adminRepository.save(Admin.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .build());
        }
    }
}
