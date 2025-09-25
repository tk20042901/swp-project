package com.swp.project.repository.order;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.user.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Page<Order> searchByCustomer_EmailContainsAndOrderDateBetween(String customer_email, LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);

    Page<Order> searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderDateBetween(Long statusId,String customer_email,LocalDateTime toDate, LocalDateTime fromDate, Pageable pageable);


    List<Order> findByCustomer(Customer customer);

}
