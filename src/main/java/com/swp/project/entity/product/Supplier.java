package com.swp.project.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Supplier implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,  length = 100)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "suppliers")
    private List<ProductBatch> productBatches;
}
