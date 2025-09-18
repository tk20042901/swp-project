package com.swp.project.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private ProductUnit unit;

    @Column(nullable = false)
    private String main_image_url;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<SubImage> sub_images;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Category> categories;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<ProductBatch> productBatches;
}
