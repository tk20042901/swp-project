package com.swp.project.service.user;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;

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
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
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
            for (int i = 1; i <= 36; i++) {
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
            if (staffDto.getId() == 0) {
                if (existsCid(staffDto.getCid())) {
                    throw new RuntimeException("Mã căn cước công dân đã được dùng");
                }
                if (userRepository.existsByEmail(staffDto.getEmail())) {
                    throw new RuntimeException("Email đã được dùng");
                }
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


    private boolean existsCid(String cid) {
        return sellerRepository.findByCid(cid) != null ||
                shipperRepository.findByCid(cid) != null ||
                managerRepository.findByCid(cid) != null;
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
        orderRepository.save(order);
    }

    public Page<Order> getDeliveringOrders(Principal principal, int page, int size) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        // Nếu repository chưa có query riêng thì vẫn phải filter trong memory
        List<Order> allOrders = orderRepository.findAll()
            .stream()
            .filter(order -> orderStatusService.isShippingStatus(order) &&
                            order.getShipper() != null &&
                            order.getShipper().getEmail().equals(principal.getName()) &&
                            orderStatusService.isShippingStatus(order))
            .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = allOrders.subList(start, end);

        return new PageImpl<>(pagedOrders, pageable, allOrders.size());
    }

    public void markOrderAsDelivered(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                                     .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        if (!orderStatusService.isShippingStatus(order)) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang giao");
        }
        orderService.updateOrderStatusToDelivered(order);
        orderRepository.save(order);
    }

}
