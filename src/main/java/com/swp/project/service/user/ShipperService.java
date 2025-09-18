package com.swp.project.service.user;

import com.swp.project.entity.user.Shipper;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.ShipperRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@Service
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    private List<Shipper> results = new ArrayList<>();

    public Shipper getByEmail(String email) { return shipperRepository.findByEmail(email);
    }

    @Transactional
    public void initShipper() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            for (int i = 1; i <= 6; i++) {
                createShipperIfNotExists(Shipper.builder()
                        .email("shipper" + i + "@shop.com")
                        .password("shipper")
                        .fullname("shipper" + i + "@shop.com")
                        .birthDate(sdf.parse("2001-09-11"))
                        .cId(UUID.randomUUID().toString())
                        .address("Pakistan")
                        .build());
            }
            createShipperIfNotExists(Shipper.builder()
                    .email("disabled-shipper@shop.com")
                    .password("shipper")
                    .fullname("seller" + 999 + "@shop.com")
                    .birthDate(sdf.parse("2001-09-11"))
                    .cId(UUID.randomUUID().toString())
                    .address("Pakistan")
                    .enabled(false)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createShipperIfNotExists(Shipper shipper) {
        if (!shipperRepository.existsByEmail(shipper.getEmail())) {
            shipper.setPassword(passwordEncoder.encode(shipper.getPassword()));
            shipperRepository.save(shipper);
        }
    }

    public void findAllShippers() {
        results = shipperRepository.findAll();
    }

    public void save(Shipper shipper) { shipperRepository.save(shipper);
    }


    public Shipper getShipperById(Long id) {
        return shipperRepository.findById(id).orElse(null);
    }

    @Transactional
    public void setShipperStatus(Long id, boolean status) {
        Shipper shipper = getShipperById(id);
        shipper.setEnabled(status);

        if (!status) {
            eventPublisher.publishEvent(new UserDisabledEvent(shipper.getEmail()));
        }

        shipperRepository.save(shipper);
    }

    public void sortBy(String columnName, int k) {
        switch (columnName) {
            case "id":
                results.sort((o1, o2) -> k * o1.getId().compareTo(o2.getId()));
                break;
            case "email":
                results.sort((o1, o2) -> k * o1.getUsername().compareTo(o2.getUsername()));
                break;
            case "fullname":
                results.sort((o1, o2) -> k * o1.getFullname().compareTo(o2.getFullname()));
                break;
            case "cId":
                results.sort((o1, o2) -> k * o1.getCId().compareTo(o2.getCId()));
                break;
            case "address":
                results.sort((o1, o2) -> k * o1.getAddress().compareTo(o2.getAddress()));
                break;
            case "enabled":
                results.sort((o1, o2) -> {
                    int tempO1IsEnabled = o1.isEnabled() ? 1 : 0;
                    int tempO2IsEnabled = o2.isEnabled() ? 1 : 0;
                    return k * (tempO1IsEnabled - tempO2IsEnabled);
                });
                break;
        }
    }
}
