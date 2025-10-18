package com.swp.project.repository.product;


import com.swp.project.entity.product.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch,Long> {

    List<ProductBatch> getByProduct_IdAndExpiredDateAfter(Long productId, LocalDateTime expiredDateAfter);
}
