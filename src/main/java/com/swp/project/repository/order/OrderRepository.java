package com.swp.project.repository.order;

import java.time.LocalDateTime;
import java.util.List;

import com.swp.project.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Shipper;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> searchByCustomer_EmailContainsAndOrderAtBetween(String customer_email, LocalDateTime toDate,
                                                                LocalDateTime fromDate, Pageable pageable);

    Page<Order> searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderAtBetween(Long statusId, String customer_email, LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    Long countByShipperAndOrderStatus(Shipper shipper, OrderStatus orderStatus);

    List<Order> findByOrderStatusAndPaymentExpiredAtBefore(OrderStatus pendingPaymentStatus, LocalDateTime now);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    Page<Order> findByCustomerAndOrderAtBetween(Customer customer,LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Page<Order> findByCustomerAndOrderStatus_Name(Customer customer, String orderStatusName, Pageable pageable);

    Page<Order> findByCustomerAndOrderAtAfter(Customer customer, LocalDateTime from, Pageable pageable);

    Page<Order> findByCustomerAndOrderAtBefore(Customer customer, LocalDateTime to, Pageable pageable);

    Page<Order> findByCustomerAndOrderStatus_NameAndOrderAtBetween(Customer customer, String orderStatusName, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    List<Order> findByShipper_Email(String shipperEmail);

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
      WHERE (
            (oi.order.orderStatus.name = 'Đã Giao Hàng' AND oi.order.paymentMethod.id = 'COD')
         OR (oi.order.orderStatus.name IN ('Đang Chuẩn Bị Hàng', 'Đang Giao Hàng', 'Đã Giao Hàng')
             AND oi.order.paymentMethod.id = 'QR')
          )
        And function('DATE',oi.order.orderAt)= current_date
""")
    Long getRevenueToday();

    @Query("""
    SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
    FROM OrderItem oi
    WHERE (
            (oi.order.orderStatus.name = 'Đã Giao Hàng' AND oi.order.paymentMethod.id = 'COD')
         OR (oi.order.orderStatus.name IN ('Đang Chuẩn Bị Hàng', 'Đang Giao Hàng', 'Đã Giao Hàng')
             AND oi.order.paymentMethod.id = 'QR')
          )
      AND oi.order.orderAt >= FUNCTION('DATE_TRUNC', 'week', CURRENT_TIMESTAMP)
""")
    Long getRevenueThisWeek();


    @Query("""
        SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
        FROM OrderItem oi
        WHERE (
            (oi.order.orderStatus.name = 'Đã Giao Hàng' AND oi.order.paymentMethod.id = 'COD')
         OR (oi.order.orderStatus.name IN ('Đang Chuẩn Bị Hàng', 'Đang Giao Hàng', 'Đã Giao Hàng')
             AND oi.order.paymentMethod.id = 'QR')
          )
          AND oi.order.orderAt >= FUNCTION('DATE_TRUNC', 'month', CURRENT_DATE)
    """)
    Long getRevenueThisMonth();

    @Query("""
        SELECT pb
        FROM Product pb
        WHERE pb.quantity <= :unitSoldOut
        ORDER BY pb.quantity ASC
    """)
    List<Product> findingNearlySoldOutProduct(@Param("unitSoldOut") int unitSoldOut);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
        FROM OrderItem oi
        JOIN oi.order o
          WHERE (
            (oi.order.orderStatus.name = 'Đã Giao Hàng' AND oi.order.paymentMethod.id = 'COD')
         OR (oi.order.orderStatus.name IN ('Đang Chuẩn Bị Hàng', 'Đang Giao Hàng', 'Đã Giao Hàng')
             AND oi.order.paymentMethod.id = 'QR')
          )
        AND o.orderAt BETWEEN :start AND :end
""")
    Long getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
    SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
    FROM OrderItem oi
      WHERE (
            (oi.order.orderStatus.name = 'Đã Giao Hàng' AND oi.order.paymentMethod.id = 'COD')
         OR (oi.order.orderStatus.name IN ('Đang Chuẩn Bị Hàng', 'Đang Giao Hàng', 'Đã Giao Hàng')
             AND oi.order.paymentMethod.id = 'QR')
          )
      AND function('DATE', oi.order.orderAt) = :date
""")
    Long getRevenueByDate(@Param("date") LocalDateTime date);


    Page<Order> findByCustomerAndOrderStatus_NameAndOrderAtAfter(Customer customer, String orderStatus, LocalDateTime fromDate, Pageable pageable);

    Page<Order> findByCustomerAndOrderStatus_NameAndOrderAtBefore(Customer customer, String orderStatusName, LocalDateTime toDate, Pageable pageable);

    @Query("""
        SELECT FUNCTION('TO_CHAR', o.orderAt, 'YYYY-MM-DD'),
               SUM(oi.quantity * p.price)
        FROM Order o
        JOIN o.orderItem oi
        JOIN oi.product p
        GROUP BY FUNCTION('TO_CHAR', o.orderAt, 'YYYY-MM-DD')
        ORDER BY FUNCTION('TO_CHAR', o.orderAt, 'YYYY-MM-DD')
    """)
    List<Object[]> getRevenueLastDays();

    @Query(value = """
        SELECT
            TO_CHAR(d::date, 'YYYY-MM-DD') AS date,
            COALESCE(SUM(oi.quantity * p.price), 0) AS revenue
        FROM generate_series(CURRENT_DATE - INTERVAL '7 day', CURRENT_DATE, '1 day') d
        LEFT JOIN orders o ON DATE(o.order_at) = d::date
        LEFT JOIN order_item oi ON o.id = oi.order_id
        LEFT JOIN product p ON oi.product_id = p.id
        GROUP BY d
        ORDER BY d desc
    """, nativeQuery = true)
    List<Object[]> getRevenueLast7Days();


    @Query(value = """
    SELECT
    TO_CHAR(d::date,'YYYY/MM') AS month,
    COALESCE(SUM(oi.quantity * p.price),0) AS revenue
    FROM generate_series(CURRENT_DATE - INTERVAL '12 month', CURRENT_DATE, '1 month') d
    LEFT JOIN orders o ON DATE_TRUNC('month', o.order_at) = DATE_TRUNC('month', d::date)
    LEFT JOIN order_item oi ON o.id = oi.order_id
    LEFT JOIN product p ON oi.product_id = p.id
    GROUP BY d
    ORDER BY d desc
    """, nativeQuery = true)
    List<Object[]> getRevenueLast12Months();
}

