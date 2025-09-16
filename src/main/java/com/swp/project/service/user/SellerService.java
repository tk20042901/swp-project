package com.swp.project.service.user;

import com.swp.project.entity.user.Seller;
import com.swp.project.repository.user.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initSeller() {
        for (int i = 1; i <= 36; i++) {
            createSellerIfNotExists(Seller.builder()
                    .email("seller" + i + "@shop.com")
                    .password("seller")
                    .build());
        }
        createSellerIfNotExists(Seller.builder()
                .email("disabled-seller@shop.com")
                .password("seller")
                .enabled(false)
                .build());
    }


    private void createSellerIfNotExists(Seller seller) {
        if (!sellerRepository.existsByEmail(seller.getEmail())) {
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
            sellerRepository.save(seller);
        }
    }

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }
}
