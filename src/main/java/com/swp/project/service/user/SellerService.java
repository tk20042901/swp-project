package com.swp.project.service.user;

import com.swp.project.entity.user.Seller;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final ApplicationEventPublisher eventPublisher;
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

    public Seller getByEmail(String email) { return sellerRepository.findByEmail(email);
    }

    public void save(Seller seller) { sellerRepository.save(seller);
    }

    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void setSellerStatus(Long id, boolean status) {
        Seller seller = getSellerById(id);
        seller.setEnabled(status);

        if (!status) {
            eventPublisher.publishEvent(new UserDisabledEvent(seller.getEmail()));
        }

        sellerRepository.save(seller);
    }
}
