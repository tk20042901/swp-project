package com.swp.project.entity.order;


import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    private Shipper shipper;

    @ManyToOne(fetch = FetchType.EAGER)
    private Seller seller;

    @ManyToOne(fetch = FetchType.EAGER)
    private OrderStatus orderStatus;

    private Instant orderDate;
}
