package com.swp.project.repository.order;

import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderItemId;
import com.swp.project.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    List<OrderItem> getByProduct_IdAndOrder_OrderStatus(Long productId, OrderStatus orderOrderStatus);
}
