package com.swp.project.service.user;

import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.User;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.SellerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    private List<Seller> results = new ArrayList<>();

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

    public void findAllSellers() {
        results = sellerRepository.findAll();
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

    public void sortBy(String columnName, int k) {
        switch (columnName) {
            case "id":
                results.sort((o1, o2) -> {
                    User tempO1 = (User) o1;
                    User tempO2 = (User) o2;
                    return k * tempO1.getId().compareTo(tempO2.getId());
                });
                break;
            case "username":
                results.sort((o1, o2) -> {
                    User tempO1 = (User) o1;
                    User tempO2 = (User) o2;
                    return k * tempO1.getUsername().compareTo(tempO2.getUsername());
                });
                break;
            case "enabled":
                results.sort((o1, o2) -> {
                    User tempO1 = (User) o1;
                    User tempO2 = (User) o2;
                    int tempO1IsEnabled = tempO1.isEnabled() ? 1 : 0;
                    int tempO2IsEnabled = tempO2.isEnabled() ? 1 : 0;
                    return k * (tempO1IsEnabled - tempO2IsEnabled);
                });
                break;
        }
    }
}
