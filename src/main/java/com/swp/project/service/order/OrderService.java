package com.swp.project.service.order;

import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.repository.user.CustomerRepository;
import com.swp.project.service.AddressService;
import com.swp.project.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductService productService;
    private final OrderStatusService orderStatusService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final AddressService addressService;

    public Page<Order> getAllOrder() {
        Pageable pageable = PageRequest.of(0,10, Sort.by("id").ascending());
        return orderRepository.findAll(pageable);
    }

    public Page<Order> searchOrder(SellerSearchOrderDto sellerSearchOrderDto) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(sellerSearchOrderDto.getGoToPage()) - 1,
                10,
                Sort.by("id").ascending());
        if (sellerSearchOrderDto.getStatusId() == null || sellerSearchOrderDto.getStatusId() == 0) {
            return orderRepository.searchByCustomer_EmailContainsAndOrderTimeBetween(
                    sellerSearchOrderDto.getCustomerEmail() == null
                            ? ""
                            : sellerSearchOrderDto.getCustomerEmail(),
                    sellerSearchOrderDto.getFromDate() == null
                            ? LocalDate.parse("2005-06-03").atStartOfDay()
                            : sellerSearchOrderDto.getFromDate().atStartOfDay(),
                    sellerSearchOrderDto.getToDate() == null
                            ? LocalDateTime.now()
                            : sellerSearchOrderDto.getToDate().atTime(23,59),
                    pageable);
        }
        return orderRepository.searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderTimeBetween(
                sellerSearchOrderDto.getStatusId(),
                sellerSearchOrderDto.getCustomerEmail() == null
                        ? ""
                        : sellerSearchOrderDto.getCustomerEmail(),
                sellerSearchOrderDto.getFromDate() == null
                        ? LocalDate.parse("2005-06-03").atStartOfDay()
                        : sellerSearchOrderDto.getFromDate().atStartOfDay(),
                sellerSearchOrderDto.getToDate() == null
                        ? LocalDateTime.now()
                        : sellerSearchOrderDto.getToDate().atTime(23,59),
                pageable);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public void setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        assert order != null;
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void createCodOrder(String customerEmail,
                                List<ShoppingCartItem> shoppingCartItems,
                                DeliveryInfoDto deliveryInfoDto) {

        if(shoppingCartItems.stream().anyMatch(i ->
                i.getQuantity() > productService.getAvailableQuantity(i.getProduct().getId()))) {
            throw new RuntimeException("Số lượng sản phẩm trong đơn hàng vượt quá số lượng khả dụng");
        }

        shoppingCartItems.forEach(i -> shoppingCartItemRepository
                .deleteByCustomerEmailAndProductId(customerEmail, i.getProduct().getId()));

        Order order = orderRepository.save(Order.builder()
                .orderStatus(orderStatusService.getPendingConfirmationStatus())
                .fullName(deliveryInfoDto.getFullName())
                .phoneNumber(deliveryInfoDto.getPhone())
                .communeWard(addressService.getCommuneWardByCode(deliveryInfoDto.getCommuneWardCode()))
                .specificAddress(deliveryInfoDto.getSpecificAddress())
                .customer(customerRepository.getByEmail(customerEmail))
                .build());
        List<OrderItem> orderItems = shoppingCartItems.stream().map(cartItem ->
                        OrderItem.builder()
                                .order(order)
                                .product(cartItem.getProduct())
                                .quantity(cartItem.getQuantity())
                                .build()).collect(Collectors.toList());
        order.setOrderItem(orderItems);
        orderRepository.save(order);
    }

    @Transactional
    public Order createQrOrder(String customerEmail,
                                List<ShoppingCartItem> shoppingCartItems,
                                DeliveryInfoDto deliveryInfoDto) {

        if(shoppingCartItems.stream().anyMatch(i ->
                i.getQuantity() > productService.getAvailableQuantity(i.getProduct().getId()))) {
            throw new RuntimeException("Số lượng sản phẩm trong đơn hàng vượt quá số lượng khả dụng");
        }

        shoppingCartItems.forEach(i -> shoppingCartItemRepository
                .deleteByCustomerEmailAndProductId(customerEmail, i.getProduct().getId()));

        Order order = orderRepository.save(Order.builder()
                .paymentExpiredTime(LocalDateTime.now().plusMinutes(15)) // QR expires in 15 minutes
                .orderStatus(orderStatusService.getPendingPaymentStatus())
                .fullName(deliveryInfoDto.getFullName())
                .phoneNumber(deliveryInfoDto.getPhone())
                .communeWard(addressService.getCommuneWardByCode(deliveryInfoDto.getCommuneWardCode()))
                .specificAddress(deliveryInfoDto.getSpecificAddress())
                .customer(customerRepository.getByEmail(customerEmail))
                .build());
        List<OrderItem> orderItems = shoppingCartItems.stream().map(cartItem ->
                        OrderItem.builder()
                                .order(order)
                                .product(cartItem.getProduct())
                                .quantity(cartItem.getQuantity())
                                .build()).collect(Collectors.toList());
        order.setOrderItem(orderItems);
        return orderRepository.save(order);
    }

    @Scheduled(fixedRate = 300000) // cancel expired qr orders every 5 minutes
    @Transactional
    public void cancelExpiredQrOrders() {
        List<Order> expiredOrders = orderRepository.findByOrderStatusAndPaymentExpiredTimeBefore(
                orderStatusService.getPendingPaymentStatus(), LocalDateTime.now());
        expiredOrders.forEach(order -> setOrderStatus(order.getId(), orderStatusService.getCancelledStatus()));
        orderRepository.saveAll(expiredOrders);
    }

    public boolean isOrderItemQuantityMoreThanAvailable(Long orderId) {
        return getOrderById(orderId).getOrderItem().stream().anyMatch(i ->
                i.getQuantity() > productService.getAvailableQuantity(i.getProduct().getId()));
    }

    @Transactional
    public void doWhenOrderConfirmed(Long orderId) {
        pickProductForOrder(orderId);
        setOrderStatus(orderId, orderStatusService.getProcessingStatus());
    }

    @Transactional
    public void pickProductForOrder(Long orderId) {
        Order order = getOrderById(orderId);
        order.getOrderItem().forEach(item ->
                productService.pickProductInProductBatch(item.getProduct().getId(), item.getQuantity()));
    }
}
