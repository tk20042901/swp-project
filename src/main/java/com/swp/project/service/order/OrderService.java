package com.swp.project.service.order;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.swp.project.entity.order.Bill;
import com.swp.project.entity.order.shipping.Shipping;
import com.swp.project.entity.order.shipping.ShippingStatus;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.repository.order.BillRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.service.SettingService;
import com.swp.project.service.order.shipping.ShippingStatusService;
import com.swp.project.service.user.ShipperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductService productService;
    private final OrderStatusService orderStatusService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final AddressService addressService;
    private final PaymentMethodService paymentMethodService;
    private final ShippingStatusService shippingStatusService;
    private final SettingService settingService;
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final ShipperService shipperService;

    public Page<Order> getAllOrder() {
        Pageable pageable = PageRequest.of(0,10, Sort.by("id").ascending());
        return orderRepository.findAll(pageable);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public Page<Order> searchOrder(SellerSearchOrderDto sellerSearchOrderDto) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(sellerSearchOrderDto.getGoToPage()) - 1,
                10,
                Sort.by("id").ascending());
        if (sellerSearchOrderDto.getStatusId() == null || sellerSearchOrderDto.getStatusId() == 0) {
            return orderRepository.searchByCustomer_EmailContainsAndOrderAtBetween(
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
        return orderRepository.searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderAtBetween(
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
        Order order = getOrderById(orderId);
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
                .paymentMethod(paymentMethodService.getCodMethod())
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
                .paymentMethod(paymentMethodService.getQrMethod())
                .paymentExpiredAt(LocalDateTime.now().plusMinutes(15)) // QR expires in 15 minutes
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
        List<Order> expiredOrders = orderRepository.findByOrderStatusAndPaymentExpiredAtBefore(
                orderStatusService.getPendingPaymentStatus(), LocalDateTime.now());
        expiredOrders.forEach(order -> setOrderStatus(order.getId(), orderStatusService.getCancelledStatus()));
        orderRepository.saveAll(expiredOrders);
    }

    public boolean isOrderItemQuantityMoreThanAvailable(Long orderId) {
        return getOrderById(orderId).getOrderItem().stream().anyMatch(i ->
                i.getQuantity() > productService.getAvailableQuantity(i.getProduct().getId()));
    }

    @Transactional
    public void doWhenOrderConfirmed(Order order) {
        pickProductForOrder(order);
        setOrderStatus(order.getId(), orderStatusService.getProcessingStatus());
    }

    @Transactional
    public void pickProductForOrder(Order order) {
        order.getOrderItem().forEach(item ->
                productService.pickProductInProductBatch(item.getProduct().getId(), item.getQuantity()));
    }

    public void createBillForOrder(Order order) {
        Bill bill = Bill.builder()
                .paymentTime(LocalDateTime.now())
                .shopName(settingService.getShopName())
                .shopAddress(settingService.getShopAddress())
                .shopPhone(settingService.getShopPhone())
                .shopEmail(settingService.getShopEmail())
                .order(order)
                .build();
        billRepository.save(bill);
    }

    private void addShippingStatusToOrder(Order order, ShippingStatus shippingStatus) {
        order.addShippingStatus(Shipping.builder()
                .shippingStatus(shippingStatus)
                .build());
    }

    public void updateOrderStatusToShipping(Order order) {
        order.setOrderStatus(orderStatusService.getShippingStatus());
        addShippingStatusToOrder(order, shippingStatusService.getAwaitingPickupStatus());
        shipperService.autoAssignShipperToOrder(order);
        orderRepository.save(order);
    }

    public void updateOrderStatusToDelivered(Order order) {
        order.setOrderStatus(orderStatusService.getDeliveredStatus());
        addShippingStatusToOrder(order, shippingStatusService.getDeliveredStatus());
        orderRepository.save(order);
    }

    public List<Order> getSuccessOrder() {
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isDeliveredStatus(order))
                .collect(Collectors.toList());
    }

    public List<Order> getOrderByProductId(List<Order> orders, Long productId) {
        return orders.stream()
                .filter(order -> order.getOrderItem().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)))
                .collect(Collectors.toList());
    }
        public int getSoldQuantity(Long productId) {
                List<Order> orders = getOrderByProductId(getSuccessOrder(), productId);
                return orders.stream()
                        .mapToInt(order -> order.getOrderItem().stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .mapToInt(OrderItem::getQuantity)
                                .sum())
                        .sum();
        }

    public Order getOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() ->
                new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    public Long calculateTotalAmount(Order order) {
        return order.getOrderItem().stream()
                .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public long getTotalOrders(){
        return orderRepository.count();
    }
    public long getTotalDeliveredOrders(){
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isDeliveredStatus(order))
                .count();
    }

    public long getTotalProcessingOrders(){
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isProcessingStatus(order))
                .count();
    }

    public long getTotalPendingOrders(){
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isPendingConfirmationStatus(order)
                        || orderStatusService.isPendingPaymentStatus(order))
                .count();
    }

    public long getTotalCancelledOrders(){
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isCancelledStatus(order))
                .count();
    }

    public long getUnitSold(){
        long total =0;
        for(Product p : productRepository.findAll()){
            if(p.getSoldQuantity() != null)
            total += getSoldQuantity(p.getId());
        }
        return total;
    }

    public long getRevenueToday() {
        Long revenue = orderRepository.getRevenueToday();
        return revenue;
    }
    public long getRevenueThisMonth(){
        Long revenue = orderRepository.getRevenueThisMonth();
        return revenue;

    }
    public long getRevenueThisWeek() {
        Long revenue = orderRepository.getRevenueThisWeek();
        return revenue;
    }

    public List<ProductBatch> getNearlyExpiredProduct(){
        return orderRepository.findingNearlyExpiredProduct();
    }

    public List<ProductBatch> getNearlySoldOutProduct(){
        int unitsoldOut = 20;
        return orderRepository.findingNearlySoldOutProduct(unitsoldOut);
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

        // Update order status to delivered directly instead of calling OrderService
        order.setOrderStatus(orderStatusService.getDeliveredStatus());
        order.addShippingStatus(Shipping.builder()
                .shippingStatus(shippingStatusService.getDeliveredStatus())
                .build());
        orderRepository.save(order);
    }
}
