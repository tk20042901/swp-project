package com.swp.project.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"products"})
@Entity
public class Category implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "categories")
    private List<Product> products;
}
