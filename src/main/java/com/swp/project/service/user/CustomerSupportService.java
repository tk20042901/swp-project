package com.swp.project.service.user;

import com.swp.project.entity.user.CustomerSupport;
import com.swp.project.repository.user.CustomerSupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerSupportService {

    private final CustomerSupportRepository customerSupportRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initCustomerSupport() {
        for (int i = 1; i <= 9; i++) {
            createCustomerSupportIfNotExists(CustomerSupport.builder()
                    .email("customer-support" + i + "@shop.com")
                    .password("customer-support")
                    .build());
        }
        createCustomerSupportIfNotExists(CustomerSupport.builder()
                .email("disabled-customer-support@shop.com")
                .password("customer-support")
                .enabled(false)
                .build());
    }


    private void createCustomerSupportIfNotExists(CustomerSupport customerSupport) {
        if (!customerSupportRepository.existsByEmail(customerSupport.getEmail())) {
            customerSupport.setPassword(passwordEncoder.encode(customerSupport.getPassword()));
            customerSupportRepository.save(customerSupport);
        }
    }
}
