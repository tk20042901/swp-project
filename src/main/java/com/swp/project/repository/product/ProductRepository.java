package com.swp.project.repository.product;

import com.swp.project.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    boolean existsByName(String name);

    Product findByName(String productName);


    List<Product> getByName(String name);
}
