package com.swp.project.entity.order;


import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Shipper;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> orderItem = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Instant orderDate;

    @Column(length = 50)
    private String fullName;

    @Column(length = 15)
    private String phoneNumber;

    @ManyToOne(fetch =  FetchType.EAGER)
    private CommuneWard communeWard;

    @Column(length = 100)
    private String specificAddress;
}
