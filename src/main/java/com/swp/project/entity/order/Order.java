package com.swp.project.entity.order;


import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.order.shipping.Shipping;
import com.swp.project.entity.user.Customer;
import com.swp.project.entity.user.Shipper;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order{
    @Id
    @Builder.Default
    private Long id = ThreadLocalRandom.current().nextLong(10000,100000);

    @ManyToOne(fetch = FetchType.EAGER)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    private Shipper shipper;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> orderItem = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime orderAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    private PaymentMethod paymentMethod;

    private String paymentLink;

    private LocalDateTime paymentExpiredAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Shipping> shipping;

    @Column(length = 50)
    private String fullName;

    @Column(length = 15)
    private String phoneNumber;

    @ManyToOne(fetch =  FetchType.EAGER)
    private CommuneWard communeWard;

    @Column(length = 100)
    private String specificAddress;

    public void addShippingStatus(Shipping shippingStatus){
        shipping.add(shippingStatus);
    }

    public int getTotalAmount(){
        return orderItem.stream()
                        .mapToInt(od -> od.getProduct().getPrice() * od.getQuantity()).sum();
    }

    public String getAddressString(){
        return specificAddress + ", " + communeWard.getName() + ", " + communeWard.getProvinceCity().getName();
    }
}
