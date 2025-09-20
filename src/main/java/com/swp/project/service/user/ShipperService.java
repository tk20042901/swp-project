package com.swp.project.service.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.swp.project.repository.user.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.user.Shipper;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.address.ProvinceCityRepository;
import com.swp.project.service.AddressService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Service
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final SellerRepository sellerRepository;
    private final CustomerSupportRepository  customerSupportRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final CommuneWardRepository communeWardRepository;
    private final ProvinceCityRepository provinceCityRepository;


    private List<Shipper> results = new ArrayList<>();

    public Shipper getByEmail(String email) { return shipperRepository.findByEmail(email);
    }

    @Transactional
    public void initShipper() {
        try {
            for (int i = 1; i <= 6; i++) {
                createShipperIfNotExists(Shipper.builder()
                        .email("shipper" + i + "@shop.com")
                        .password("shipper")
                        .fullname("shipper" + i + "@shop.com")
                        .birthDate(sdf.parse("2001-09-11"))
                        .cid(UUID.randomUUID().toString())
                        .specificAddress("123 Đường ABC, Phường XYZ")
                        .build());
            }
            createShipperIfNotExists(Shipper.builder()
                    .email("disabled-shipper@shop.com")
                    .password("shipper")
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


    private void createShipperIfNotExists(Shipper shipper) {
        if (!userRepository.existsByEmail(shipper.getEmail())) {
            shipper.setPassword(passwordEncoder.encode(shipper.getPassword()));
            shipperRepository.save(shipper);
        }
    }

    public void findAll() {
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
            case "cid":
                results.sort((o1, o2) -> k * o1.getCid().compareTo(o2.getCid()));
                break;
            case "address":
                results.sort((o1, o2) -> k * o1.getCommuneWard().toString().compareTo(o2.getCommuneWard().toString()));
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

    public void add(StaffDto staffDto) {
        if (staffDto != null) {
            if (existsCid(staffDto.getCid())) {
                throw new RuntimeException("Mã căn cước công dân đã được dùng");
            }
            if (userRepository.existsByEmail(staffDto.getEmail())) {
                throw new RuntimeException("Email đã được dùng");
            }
            Shipper shipper;
            try {
                shipper = Shipper.builder()
                        .email(staffDto.getEmail())
                        .password(staffDto.getPassword())
                        .fullname(staffDto.getFullname())
                        .birthDate(sdf.parse(staffDto.getBirthDate()))
                        .cid(staffDto.getCid())
                        .communeWard(addressService.getCommuneWardByCode(staffDto.getCommuneWard()))
                        .specificAddress(staffDto.getSpecificAddress())
                        .build();
            } catch (ParseException e) {
                throw new RuntimeException("Định dạng ngày tháng năm bất thường");
            }

            shipperRepository.save(shipper);

        }
    }


    private boolean existsCid(String Cid) {
        return sellerRepository.findByCid(Cid) != null ||
                shipperRepository.findByCid(Cid) != null ||
                customerSupportRepository.findByCid(Cid) != null
//                ||
//                managerRepository.findBycId(Cid) != null
                ;
    }

    public void findByNameAndCid(String name, String cid) {
        if ((name == null || name.isEmpty()) && (cid == null || cid.isEmpty())) {
            System.out.println("thuc hien findAll");
            results = shipperRepository.findAll();
        }
        else {
            results = shipperRepository.findByFullnameContainsAndCidContains(name, cid);
        }
    }
}
