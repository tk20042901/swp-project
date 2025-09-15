package com.swp.project.entity.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,  length = 100)
    @Nationalized
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "suppliers")
    private List<ProductBatch> productBatches;
}
