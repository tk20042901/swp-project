package com.swp.project.service.order;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.entity.order.Bill;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.order.shipping.Shipping;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.repository.order.BillRepository;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.repository.user.CustomerRepository;
import com.swp.project.service.AddressService;
import com.swp.project.service.SettingService;
import com.swp.project.service.order.shipping.ShippingStatusService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.ShipperService;

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

    private List<Order> results = List.of();

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
        setOrderStatus(order.getId(), orderStatusService.getProcessingStatus());
        pickProductForOrder(order);
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

    public List<Order> getSuccessOrder() {
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isDeliveredStatus)
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
                .mapToLong(item -> (long) item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public long getTotalOrders(){
        return orderRepository.count();
    }
    public long getTotalDeliveredOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isDeliveredStatus)
                .count();
    }

    public long getTotalProcessingOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isProcessingStatus)
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
                .filter(orderStatusService::isCancelledStatus)
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
        return orderRepository.getRevenueToday();
    }
    public long getRevenueThisMonth(){
        return orderRepository.getRevenueThisMonth();

    }
    public long getRevenueThisWeek() {
        return orderRepository.getRevenueThisWeek();
    }

    public List<ProductBatch> getNearlyExpiredProduct(){
        return orderRepository.findingNearlyExpiredProduct();
    }

    public List<ProductBatch> getNearlySoldOutProduct(){
        int unitsoldOut = 20;
        return orderRepository.findingNearlySoldOutProduct(unitsoldOut);
    }

    public long getRevenueYesterday(){
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return orderRepository.getRevenueByDate(yesterday);
    }

    public long getRevenueLastWeek(){
        LocalDateTime startOfThisWeek = LocalDateTime.now().with(DayOfWeek.MONDAY);
        LocalDateTime lastWeek = startOfThisWeek.minusWeeks(1);
        LocalDateTime endOfWeek = lastWeek.plusDays(6);
        return orderRepository.getRevenueBetween(lastWeek, endOfWeek);
    }

    public long getRevenueLastMonth(){
        LocalDateTime startOfThisMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfMonth = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth());
        return orderRepository.getRevenueBetween(lastMonth, endOfMonth);
    }
    public double getDailyPercentageChange(){
        long today = getRevenueToday();
        long yesterday = getRevenueYesterday();
        if(yesterday == 0){
            return 100.0;
        }
        if(today == 0){
            return 0;
        }
        double percentageChange = ((double)(today - yesterday) / yesterday) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }

    public double getWeeklyPercentageChange(){
        long thisWeek = getRevenueThisWeek();
        long lastWeek = getRevenueLastWeek();
        if(lastWeek == 0) {
            return 100.0;
        }
        if(thisWeek == 0) {
            return 0;
        }
        double percentageChange = ((double)(thisWeek - lastWeek) / lastWeek) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }

    public double getMonthlyPercentageChange(){
        long thisMonth = getRevenueThisMonth();
        long lastMonth = getRevenueLastMonth();
        if(lastMonth == 0) {
            return 100.0;
        }
        if(thisMonth == 0) {
            return 0;
        }
        double percentageChange = ((double)(thisMonth - lastMonth) / lastMonth) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }


    public void markOrderStatusAsShipping(Order order) {
        order.setOrderStatus(orderStatusService.getShippingStatus());
        order.addShippingStatus(Shipping.builder()
                .shippingStatus(shippingStatusService.getAwaitingPickupStatus())
                .build());
        shipperService.autoAssignShipperToOrder(order);
        orderRepository.save(order);
    }

    public void markOrderShippingStatusAsPickedUp(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        if (!shippingStatusService.isAwaitingPickupStatus(order.getCurrentShippingStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang lấy hàng");
        }

        try {
            // Update shipping status to picked up
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getPickedUpStatus())
                    .build());
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
    }

    public void markOrderShippingStatusAsShipping(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        if (!shippingStatusService.isPickedUpStatus(order.getCurrentShippingStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đã lấy hàng");
        }

        try {
            // Update shipping status to shipping
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getShippingStatus())
                    .build());
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
    }

    public void markOrderStatusAsDelivered(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        if (!shippingStatusService.isShippingStatus(order.getCurrentShippingStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang giao");
        }

        try {
            // Update order status to deliver directly instead of calling OrderService
            order.setOrderStatus(orderStatusService.getDeliveredStatus());
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getDeliveredStatus())
                    .build());
            orderRepository.save(order);
    
            // If COD, create bill after order is delivered
            if(paymentMethodService.isCodMethod(order.getPaymentMethod())) {
                createBillForOrder(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái hoặc tạo hóa đơn: " + e.getMessage());
        }
    }

    public int countDoneOrdersXMonthsAgo(Principal principal, int monthsAgo) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return (int) results
        .stream()
        // .filter(orderStatusService::isDeliveredStatus)
        .filter(order -> order.getCurrentShipping().getOccurredAt().getMonth() == LocalDate.now().minusMonths(monthsAgo).getMonth()
                && order.getCurrentShipping().getOccurredAt().getYear() == LocalDate.now().minusMonths(monthsAgo).getYear())
        .count();
    }

    public int countPercentageComparedToThePreviousMonth(Principal principal, int monthsAgo) {
        int currentMonthCount = countDoneOrdersXMonthsAgo(principal, monthsAgo);
        int previousMonthCount = countDoneOrdersXMonthsAgo(principal, monthsAgo + 1);

        if (previousMonthCount == 0) {
            return currentMonthCount == 0 ? 0 : 100; // If both are 0, return 0%, else return 100%
        }

        return (int) (((double) (currentMonthCount - previousMonthCount) / previousMonthCount) * 100);
    }

    public int countDoneOrdersXDaysAgo(Principal principal, int daysAgo) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return (int) results
        .stream()
        // .filter(orderStatusService::isDeliveredStatus)
        .filter(order -> order.getCurrentShipping().getOccurredAt().getDayOfYear() == LocalDate.now().minusDays(daysAgo).getDayOfYear()
                && order.getCurrentShipping().getOccurredAt().getYear() == LocalDate.now().minusDays(daysAgo).getYear())
        .count();
    }

    public int countPercentageComparedToThePreviousDay(Principal principal, int daysAgo) {
        int currentDayCount = countDoneOrdersXDaysAgo(principal, daysAgo);
        int previousDayCount = countDoneOrdersXDaysAgo(principal, daysAgo + 1);

        if (previousDayCount == 0) {
            return currentDayCount == 0 ? 0 : 100; // If both are 0, return 0%, else return 100%
        }

        return (int) (((double) (currentDayCount - previousDayCount) / previousDayCount) * 100);
    }

    public Page<Order> getDeliveringOrders(Principal principal, int page, int size, String searchQuery, String sortCriteria, int k) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        // Nếu repository chưa có query riêng thì vẫn phải filter trong memory
        List<Order> allOrders = orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(order -> orderStatusService.isShippingStatus(order))
            .sorted((o1, o2) -> {
                if (sortCriteria == null) return 0;
                return switch (sortCriteria) {
                    case "id" -> k * o1.getId().compareTo(o2.getId());
                    case "email" -> k * o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                    case "status" ->
                            k * o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                    default -> 0;
                };
            })
            .toList();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            allOrders = allOrders.stream()
                .filter(order -> order.getCustomer().getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = allOrders.subList(start, end);

        return new PageImpl<>(pagedOrders, pageable, allOrders.size());
    }

    public Page<Order> getDoneOrders(Principal principal, int pageDone, int size, String searchQuery, String sortCriteria, int k) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Pageable pageable = PageRequest.of(pageDone - 1, size);

        // Nếu repository chưa có query riêng thì vẫn phải filter trong memory
        List<Order> allOrders = orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(order -> orderStatusService.isDeliveredStatus(order))
            .sorted((o1, o2) -> {
                if (sortCriteria == null) return 0;
                return switch (sortCriteria) {
                    case "id" -> k * o1.getId().compareTo(o2.getId());
                    case "email" -> k * o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                    case "status" ->
                            k * o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                    default -> 0;
                };
            })
            .toList();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            allOrders = allOrders.stream()
                .filter(order -> order.getCustomer().getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = allOrders.subList(start, end);

        return new PageImpl<>(pagedOrders, pageable, allOrders.size());
    }

    public void loadDoneOrders(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        results = orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(order -> orderStatusService.isDeliveredStatus(order))
            .toList();

    }

    public String getShippedAt(Order order){
        if (order.getShipping() == null || order.getShipping().isEmpty()) return null;
        if (shippingStatusService.isDeliveredStatus(order.getCurrentShippingStatus())) {
            LocalDateTime occurredAt = order.getCurrentShipping().getOccurredAt();
            return "Ngày " + occurredAt.getDayOfMonth() + " tháng " + occurredAt.getMonthValue() + " năm " + occurredAt.getYear() + 
                    " lúc " + String.format("%02d", occurredAt.getHour()) + ":" + String.format("%02d", occurredAt.getMinute());
        }
        return "Chưa giao";
    }

}
