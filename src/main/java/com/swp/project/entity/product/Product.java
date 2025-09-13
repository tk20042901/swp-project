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
    private String name;
    private String description;
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private ProductUnit unit;

    private String main_image_url;

    @Builder.Default
    private boolean enabled = true;

    @OneToMany(mappedBy = "product")
    private List<SubImage>  sub_images;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Category> categories;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Supplier> suppliers;

    @OneToMany(mappedBy = "product")
    private List<ProductBatch> productBatches;
}
