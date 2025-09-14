package com.swp.project.repository.product;

import com.swp.project.entity.product.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier,Long> {

    boolean existsByName(String name);
}
