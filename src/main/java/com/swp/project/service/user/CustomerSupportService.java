package com.swp.project.service.user;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.user.CustomerSupport;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.*;
import com.swp.project.service.AddressService;
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
public class CustomerSupportService {

    private final CustomerSupportRepository customerSupportRepository;
    private final SellerRepository sellerRepository;
    private final ShipperRepository shipperRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private List<CustomerSupport> results = new ArrayList<>();

    @Transactional
    public void initCustomerSupport() {
        try {
            for (int i = 1; i <= 36; i++) {
                createCustomerSupportIfNotExists(CustomerSupport.builder()
                        .email("customer-support" + i + "@shop.com")
                        .password("customer-support")
                        .fullname("customer-support" + i + "@shop.com")
                        .birthDate(sdf.parse("2001-09-11"))
                        .cid(UUID.randomUUID().toString())
                        .specificAddress("123 Đường ABC, Phường XYZ")
                        .build());
            }
            createCustomerSupportIfNotExists(CustomerSupport.builder()
                    .email("disabled-customer-support@shop.com")
                    .password("customer-support")
                    .fullname("customer-support" + 999 + "@shop.com")
                    .birthDate(sdf.parse("2001-09-11"))
                    .cid(UUID.randomUUID().toString())
                    .specificAddress("123 Đường ABC, Phường XYZ")
                    .enabled(false)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void createCustomerSupportIfNotExists(CustomerSupport customerSupport) {
        if (!userRepository.existsByEmail(customerSupport.getEmail())) {
            customerSupport.setPassword(passwordEncoder.encode(customerSupport.getPassword()));
            customerSupportRepository.save(customerSupport);
        }
    }

    public void findAll() {
        results = customerSupportRepository.findAll();
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
            if (staffDto.getId() == 0) {
                if (existscId(staffDto.getCid())) {
                    throw new RuntimeException("Mã căn cước công dân đã được dùng");
                }
                if (userRepository.existsByEmail(staffDto.getEmail())) {
                    throw new RuntimeException("Email đã được dùng");
                }
            }

            CustomerSupport customerSupport;
            try {
                customerSupport = CustomerSupport.builder()
                        .id(staffDto.getId() != 0 ? staffDto.getId() : null)
                        .email(staffDto.getEmail())
                        .password(staffDto.getPassword())
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
            customerSupportRepository.save(customerSupport);

        }
    }

    public CustomerSupport getByEmail(String email) {
        return customerSupportRepository.findByEmail(email);
    }

    public void save(CustomerSupport customerSupport) {
        customerSupportRepository.save(customerSupport);
    }

    @Transactional
    public void setCustomerSupportStatus(Long id, boolean status) {
        CustomerSupport customerSupport = getCustomerSupportById(id);
        customerSupport.setEnabled(status);

        if (!status) {
            eventPublisher.publishEvent(new UserDisabledEvent(customerSupport.getEmail()));
        }

        customerSupportRepository.save(customerSupport);
    }

    public CustomerSupport getCustomerSupportById(Long id) {
        return customerSupportRepository.findById(id).orElse(null);
    }


    private boolean existscId(String cid) {
        return sellerRepository.findByCid(cid) != null ||
                shipperRepository.findByCid(cid) != null ||
                customerSupportRepository.findByCid(cid) != null ||
                managerRepository.findByCid(cid) != null;
    }


    public void findByNameAndCid(String name, String cid) {
        if ((name == null || name.isEmpty()) && (cid == null || cid.isEmpty())) {
            results = customerSupportRepository.findAll();
        }
        else {
            results = customerSupportRepository.findByFullnameContainsAndCidContains(name, cid);
        }
    }


   
}
