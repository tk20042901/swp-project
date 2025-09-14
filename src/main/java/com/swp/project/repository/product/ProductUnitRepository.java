package com.swp.project.repository.product;

import com.swp.project.entity.product.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit,Integer> {
    boolean existsByName(String name);
}
