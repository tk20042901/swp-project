package com.swp.project.service.user;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.user.CustomerSupport;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.CustomerSupportRepository;
import com.swp.project.service.AddressService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@Service
public class CustomerSupportService {

    private final CustomerSupportRepository customerSupportRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private List<CustomerSupport> results = new ArrayList<>();

    @Transactional
    public void initCustomerSupport() {
        try {
            for (int i = 1; i <= 5; i++) {
                createCustomerSupportIfNotExists(CustomerSupport.builder()
                        .email("customer-support" + i + "@shop.com")
                        .password("customer-support")
                        .fullname("customer-support" + i + "@shop.com")
                        .birthDate(sdf.parse("2001-09-11"))
                        .cId(UUID.randomUUID().toString())
                        .communeWard(addressService.getCommuneWardByCode("16279"))
                        .specificAddress("123 Đường ABC, Phường XYZ")
                        .build());
            }
            createCustomerSupportIfNotExists(CustomerSupport.builder()
                    .email("disabled-customer-support@shop.com")
                    .password("customer-support")
                    .fullname("customer-support" + 999 + "@shop.com")
                    .birthDate(sdf.parse("2001-09-11"))
                    .cId(UUID.randomUUID().toString())
                    .communeWard(addressService.getCommuneWardByCode("16279"))
                    .specificAddress("123 Đường ABC, Phường XYZ")
                    .enabled(false)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void createCustomerSupportIfNotExists(CustomerSupport customerSupport) {
        if (!customerSupportRepository.existsByEmail(customerSupport.getEmail())) {
            customerSupport.setPassword(passwordEncoder.encode(customerSupport.getPassword()));
            customerSupportRepository.save(customerSupport);
        }
    }

    public void findAllCustomerSupports() {
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
            case "cId":
                results.sort((o1, o2) -> k * o1.getCId().compareTo(o2.getCId()));
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
            if (customerSupportRepository.findBycId(staffDto.getCId()) != null) {
                throw new RuntimeException("Mã căn cước công dân đã được dùng");
            }
            if (customerSupportRepository.findByEmail(staffDto.getEmail()) != null) {
                throw new RuntimeException("Email đã được dùng");
            }
            if (!staffDto.getEnabled().toLowerCase().matches("(true)|(false)")) {
                throw new RuntimeException("Trạng thái mở / khóa bất thường");
            }
            CustomerSupport customerSupport = null;
            try {
                customerSupport = CustomerSupport.builder()
                        .email(staffDto.getEmail())
                        .password(staffDto.getPassword())
                        .fullname(staffDto.getFullname())
                        .birthDate(sdf.parse(staffDto.getBirthDate()))
                        .cId(staffDto.getCId())
                        .communeWard(addressService.getCommuneWardByCode(staffDto.getCommuneWard()))
                        .specificAddress(staffDto.getSpecificAddress())
                        .enabled(Boolean.parseBoolean(staffDto.getEnabled()))
                        .build();


            } catch (ParseException e) {
                throw new RuntimeException("Định dạng ngày tháng năm bất thường");
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
}
