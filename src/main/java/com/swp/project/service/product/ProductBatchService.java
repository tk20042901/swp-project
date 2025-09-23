package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
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

    public void addProductBatch(ProductBatch productBatch) {
        productBatchRepository.save(productBatch);
    }

    public ProductBatch getProductBatchById(Long id) {
        return productBatchRepository.findById(id).orElse(null);
    }

    public void updateProductBatch(ProductBatch productBatch) {
        productBatchRepository.save(productBatch);

        eventPublisher.publishEvent(new ProductRelatedUpdateEvent
                (getProductBatchById(productBatch.getId()).getProduct().getId()));
    }

    public List<ProductBatch> getByProductId(Long productId) {
        return productBatchRepository.getByProduct_Id(productId);
    }

}
