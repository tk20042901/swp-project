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

    public List<ProductBatch> getByProductId(Long productId) {
        return productBatchRepository.getByProduct_Id(productId);
    }

    public void add(ProductBatch productBatch) {
        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProductBatch.getProduct()));
    }

    public void update(ProductBatch productBatch) {
        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProductBatch.getProduct()));
    }

}
