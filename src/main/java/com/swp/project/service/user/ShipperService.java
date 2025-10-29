package com.swp.project.service.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.user.Shipper;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.address.ProvinceCityRepository;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import com.swp.project.service.AddressService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.order.shipping.ShippingStatusService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Service
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final SellerRepository sellerRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusService orderStatusService;
    private final ShippingStatusService shippingStatusService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final CommuneWardRepository communeWardRepository;
    private final ProvinceCityRepository provinceCityRepository;


    private List<Shipper> results = new ArrayList<>();

    public Shipper getByEmail(String email) { return shipperRepository.findByEmail(email);
    }

    public void save(Shipper shipper) {
        shipperRepository.save(shipper);
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
                results.sort((o1, o2) -> {
                    int result = k * o1.getSpecificAddress().compareTo(o2.getSpecificAddress());
                    if (result == 0) {
                        result = k * o1.getId().compareTo(o2.getId());
                    }
                    return result;
                });
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
                if (existsCid(staffDto.getCid(), staffDto.getId())) {
                    throw new RuntimeException("Mã căn cước công dân đã được dùng");
                }
                if (existsEmail(staffDto.getEmail(), staffDto.getId())) {
                    throw new RuntimeException("Email đã được dùng");
                }
            Shipper shipper;
            try {
                if(staffDto.getId() == 0) {
                    shipper = Shipper.builder()
                            .email(staffDto.getEmail())
                            .password(passwordEncoder.encode(staffDto.getPassword()))
                            .fullname(staffDto.getFullname())
                            .birthDate(staffDto.getBirthDate())
                            .cid(staffDto.getCid())
                            .communeWard(addressService.getCommuneWardByCode(staffDto.getCommuneWard()))
                            .specificAddress(staffDto.getSpecificAddress())
                            .enabled(staffDto.isEnabled())
                            .build();
                } else {
                    shipper = getShipperById(staffDto.getId());
                    shipper.setEmail(staffDto.getEmail());
                    shipper.setFullname(staffDto.getFullname());
                    shipper.setBirthDate(staffDto.getBirthDate());
                    shipper.setCid(staffDto.getCid());
                    shipper.setCommuneWard(addressService.getCommuneWardByCode(staffDto.getCommuneWard()));
                    shipper.setSpecificAddress(staffDto.getSpecificAddress());
                    shipper.setEnabled(staffDto.isEnabled());
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            shipperRepository.save(shipper);

        }
    }

    private boolean existsCid(String cid, long id) {
        return (sellerRepository.findByCid(cid) != null && sellerRepository.findByCid(cid).getId() != id) ||
                (shipperRepository.findByCid(cid) != null && shipperRepository.findByCid(cid).getId() != id) ||
                (managerRepository.findByCid(cid) != null && managerRepository.findByCid(cid).getId() != id);
    }

    private boolean existsEmail(String email, long id) {
        return (sellerRepository.findByEmail(email) != null && sellerRepository.findByEmail(email).getId() != id) ||
                (shipperRepository.findByEmail(email) != null && shipperRepository.findByEmail(email).getId() != id) ||
                (managerRepository.findByEmail(email) != null && managerRepository.findByEmail(email).getId() != id);
    }

    public void findByNameAndCid(String name, String cid) {
        if ((name == null || name.isEmpty()) && (cid == null || cid.isEmpty())) {
            results = shipperRepository.findAll();
        }
        else {
            results = shipperRepository.findByFullnameContainsAndCidContains(name, cid);
        }
    }

    public void autoAssignShipperToOrder(Order order) {
        Map<Shipper, Long> shipperOrderCount = new HashMap<>();
        shipperRepository.findAll().forEach(shipper ->
                shipperOrderCount.put(shipper,
                        orderRepository.countByShipperAndOrderStatus(
                                shipper,
                                orderStatusService.getShippingStatus()
                        )
                )
        );
        Shipper assignedShipper = Collections.min
                (shipperOrderCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        order.setShipper(assignedShipper);
    }


}
