package com.swp.project.repository.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Shipper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Page<Order> searchByCustomer_EmailContainsAndOrderTimeBetween(String customer_email, LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    Page<Order> searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderTimeBetween(Long statusId, String customer_email, LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    Long countByShipperAndOrderStatus(Shipper shipper, OrderStatus orderStatus);

    List<Order> findByOrderStatusAndPaymentExpiredTimeBefore(OrderStatus pendingPaymentStatus, LocalDateTime now);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);
}
