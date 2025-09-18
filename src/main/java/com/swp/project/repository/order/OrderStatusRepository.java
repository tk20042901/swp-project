package com.swp.project.repository.order;

import com.swp.project.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus,Long> {
    OrderStatus findByName(String statusName);
}
