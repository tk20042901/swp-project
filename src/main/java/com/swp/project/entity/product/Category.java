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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Nationalized
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "categories")
    private List<Product> products;
}
