package com.swp.project.repository.order;

import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
