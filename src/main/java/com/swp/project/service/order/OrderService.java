package com.swp.project.service.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public Order getOrderById(Long orderId){
        return orderRepository.findById(orderId).orElse(null);
    }

    public void setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        assert order != null;
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    public int totalAmount(Long orderId){
        Order order = getOrderById(orderId);
        return order.getOrderItem().stream()
                .mapToInt(od -> od.getProduct().getPrice() * od.getQuantity()).sum();
    }
}
