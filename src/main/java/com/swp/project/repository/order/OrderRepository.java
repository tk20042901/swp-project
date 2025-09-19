package com.swp.project.repository.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Page<Order> getByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}
