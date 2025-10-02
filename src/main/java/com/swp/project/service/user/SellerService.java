package com.swp.project.service.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.swp.project.repository.product.ProductRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.user.Seller;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import com.swp.project.service.AddressService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final ShipperRepository shipperRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final ProductRepository productRepository;

    private List<Seller> results = new ArrayList<>();

    @Transactional
    public void initSeller() {
        try {
            for (int i = 1; i <= 36; i++) {
                createSellerIfNotExists(Seller.builder()
                        .email("seller" + i + "@shop.com")
                        .password("seller")
                        .fullname("seller" + i + "@shop.com")
                        .birthDate(sdf.parse("2001-09-11"))
                        .cid(UUID.randomUUID().toString())
                        .specificAddress("123 Đường ABC, Phường XYZ")
                        .build());
            }
            createSellerIfNotExists(Seller.builder()
                    .email("disabled-seller@shop.com")
                    .password("seller")
                    .fullname("seller" + 999 + "@shop.com")
                    .birthDate(sdf.parse("2001-09-11"))
                    .cid(UUID.randomUUID().toString())
                    .specificAddress("123 Đường ABC, Phường XYZ")
                    .enabled(false)
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void createSellerIfNotExists(Seller seller) {
        if (!userRepository.existsByEmail(seller.getEmail())) {
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
            sellerRepository.save(seller);
        }
    }

    public void findAll() {
        results = sellerRepository.findAll();
    }

    public Seller getByEmail(String email) { return sellerRepository.findByEmail(email);
    }

    public void save(Seller seller) {
        sellerRepository.save(seller);
    }

    public void add(StaffDto staffDto) {
        if (staffDto != null) {
            if (staffDto.getId() == 0) {
                if (existsCid(staffDto.getCid())) {
                    throw new RuntimeException("Mã căn cước công dân đã được dùng");
                }
                if (userRepository.existsByEmail(staffDto.getEmail())) {
                    throw new RuntimeException("Email đã được dùng");
                }
            }
            Seller seller;
            try {
                seller = Seller.builder()
                        .id(staffDto.getId() != 0 ? staffDto.getId() : null)
                        .email(staffDto.getEmail())
                        .password(staffDto.getId() != 0  ? staffDto.getEncodedPassword() : passwordEncoder.encode(staffDto.getPassword()))
                        .fullname(staffDto.getFullname())
                        .birthDate(staffDto.getBirthDate())
                        .cid(staffDto.getCid())
                        .communeWard(addressService.getCommuneWardByCode(staffDto.getCommuneWard()))
                        .specificAddress(staffDto.getSpecificAddress())
                        .enabled(staffDto.isEnabled())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            sellerRepository.save(seller);

        }
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
                results.sort((o1, o2) -> k * o1.getId().compareTo(o2.getId()));
                break;
            case "email":
                results.sort((o1, o2) -> k * o1.getUsername().compareTo(o2.getUsername()));
                break;
            case "fullname":
                results.sort((o1, o2) -> k * o1.getFullname().compareTo(o2.getFullname()));
                break;
            case "cid":
                results.sort((o1, o2) -> k * o1.getCid().compareTo(o2.getCid()));
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

    private boolean existsCid(String cid) {
        return sellerRepository.findByCid(cid) != null ||
                shipperRepository.findByCid(cid) != null ||
                managerRepository.findByCid(cid) != null;
    }

    public void findByNameAndCid(String name, String cid) {
        if ((name == null || name.isEmpty()) && (cid == null || cid.isEmpty())) {
            results = sellerRepository.findAll();
        } else {
            results = sellerRepository.findByFullnameContainsAndCidContains(name, cid);
        }
    }

    public Seller getSellerByEmail(String email) {
        return sellerRepository.findByEmail(email);
    }


}
