package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.repository.product.ProductBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductBatchService {

    private final ProductBatchRepository productBatchRepository;
    private final ApplicationEventPublisher eventPublisher;


    public ProductBatch getProductBatchById(Long id) {
        return productBatchRepository.findById(id).orElse(null);
    }

    public List<ProductBatch> getByProductId(Long productId) {
        return productBatchRepository.getByProduct_Id(productId);
    }

    public void addProductBatch(ProductBatch productBatch) {
        productBatchRepository.save(productBatch);
    }

    public void updateProductBatch(ProductBatch productBatch) {
        productBatchRepository.save(productBatch);
    }

}
