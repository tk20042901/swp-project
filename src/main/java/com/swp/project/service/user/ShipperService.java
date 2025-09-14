package com.swp.project.service.user;


import com.swp.project.entity.user.Shipper;
import com.swp.project.repository.user.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initShipper() {
        for (int i = 1; i <= 18; i++) {
            createShipperIfNotExists(Shipper.builder()
                    .email("shipper" + i + "@shop.com")
                    .password("shipper")
                    .build());
        }
        createShipperIfNotExists(Shipper.builder()
                .email("disabled-shipper@shop.com")
                .password("shipper")
                .enabled(false)
                .build());
    }


    private void createShipperIfNotExists(Shipper shipper) {
        if (!shipperRepository.existsByEmail(shipper.getEmail())) {
            shipper.setPassword(passwordEncoder.encode(shipper.getPassword()));
            shipperRepository.save(shipper);
        }
    }
}
