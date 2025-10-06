package com.swp.project.repository.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Shipper;
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

    Page<Order> searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderTimeBetween(Long statusId, String customer_email, LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    Long countByShipperAndOrderStatus(Shipper shipper, OrderStatus orderStatus);

    List<Order> findByOrderStatusAndPaymentExpiredTimeBefore(OrderStatus pendingPaymentStatus, LocalDateTime now);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0) 
        FROM Order o JOIN o.orderItem oi 
        WHERE o.orderStatus.name = 'Đã Giao Hàng'
        AND oi.product.id = :productId
            """)
    int getSoldQuantity(@Param("productId") Long productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.order.orderStatus.name='Đã Giao Hàng'")
    Long getTotalUnitSold();

   @Query("""
        SELECT coalesce(Sum(oi.quantity * oi.product.price),0)
        From OrderItem oi
        Where oi.order.orderStatus.name ='Đã giao hàng'
        And function('DATE',oi.order.orderTime)= current_date
""")
    Long getRevenueToday();

    @Query("""
        SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
        FROM OrderItem oi
        WHERE oi.order.orderStatus.name = 'Đã Giao Hàng'
          AND oi.order.orderTime >= FUNCTION('DATE_TRUNC', 'week', CURRENT_DATE)
    """)
    Long getRevenueThisWeek();


    @Query("""
        SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
        FROM OrderItem oi
        WHERE oi.order.orderStatus.name = 'Đã Giao Hàng'
          AND oi.order.orderTime >= FUNCTION('DATE_TRUNC', 'month', CURRENT_DATE)
    """)
    Long getRevenueThisMonth();

}
