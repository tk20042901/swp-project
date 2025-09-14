package com.swp.project.entity.product;

import com.swp.project.entity.user.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ProductBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant expiredDate;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    private Supplier suppliers;
}
