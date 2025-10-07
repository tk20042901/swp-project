package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.listener.event.GeminiUpdateEvent;
import com.swp.project.listener.event.GeminiUpdateEvent.UpdateType;
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
        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);

        eventPublisher.publishEvent(new GeminiUpdateEvent<ProductBatch>(savedProductBatch, UpdateType.CREATE));
    }

    public void updateProductBatch(ProductBatch productBatch) {
        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);

        eventPublisher.publishEvent(new GeminiUpdateEvent<ProductBatch>(savedProductBatch, UpdateType.UPDATE));
    }



}
