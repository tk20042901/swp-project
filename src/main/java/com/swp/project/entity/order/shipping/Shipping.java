package com.swp.project.entity.order.shipping;

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
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private ShippingStatus shippingStatus;

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
