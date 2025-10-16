package com.swp.project.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.Formula;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"productBatches", "soldQuantity", "totalQuantity"})
@Entity
public class Product implements Serializable{
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

    @Column(nullable = true)
    private String main_image_url;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<SubImage> sub_images;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Category> categories;

    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<ProductBatch> productBatches;

    
    @Transient
    private int totalQuantity;

    // Formula tính sold quantity trong DB - chỉ tính các đơn hàng đã giao thành công
    @Formula("(SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM order_item oi " +
            "INNER JOIN orders o ON o.id = oi.order_id " +
            "INNER JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE oi.product_id = id AND os.name = 'Đã Giao Hàng')")
    private Integer soldQuantity;

}
