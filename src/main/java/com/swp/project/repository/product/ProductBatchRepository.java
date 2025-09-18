package com.swp.project.repository.product;


import com.swp.project.entity.product.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch,Long> {

    List<ProductBatch> getByProduct_Id(Long productId);
}
