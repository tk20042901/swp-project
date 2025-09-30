package com.swp.project.repository.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.user.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> searchByCustomer_EmailContainsAndOrderTimeBetween(String customer_email, LocalDateTime toDate,
            LocalDateTime fromDate, Pageable pageable);

    Page<Order> searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderTimeBetween(Long statusId, String customer_email,
            LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    List<Order> findByOrderStatusAndPaymentExpiredTimeBefore(OrderStatus pendingPaymentStatus, LocalDateTime now);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0) 
        FROM Order o JOIN o.orderItem oi 
        WHERE o.orderStatus.name = 'Đã Giao Hàng'
        AND oi.product.id = :productId
            """)
    int getSoldQuantity(@Param("productId") Long productId);

}
