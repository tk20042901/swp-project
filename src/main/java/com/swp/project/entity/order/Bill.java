package com.swp.project.entity.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @Column(nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String shopAddress;

    @Column(nullable = false)
    private String shopPhone;

    @Column(nullable = false)
    private String shopEmail;

    @OneToOne(fetch = FetchType.EAGER)
    private Order order;
}
