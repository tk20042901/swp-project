package com.swp.project.service.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.user.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public Order getOrderById(Long orderId){
        return orderRepository.findById(orderId).orElse(null);
    }

    public void setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        assert order != null;
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    public Order createOrder(String customerEmail, List<ShoppingCartItem> shoppingCartItems) {
        Order order = Order.builder()
                .orderDate(Instant.now())
                .orderItem(
                        shoppingCartItems.stream().
                                map(cartItem -> OrderItem.builder()
                                        .product(cartItem.getProduct())
                                        .quantity(cartItem.getQuantity())
                                        .build())
                                .toList())
                .customer(customerRepository.getByEmail(customerEmail))
                .build();
        return orderRepository.save(order);
    }

    public int totalAmount(Long orderId){
        Order order = getOrderById(orderId);
        return order.getOrderItem().stream()
                .mapToInt(od -> od.getProduct().getPrice() * od.getQuantity()).sum();
    }
}
