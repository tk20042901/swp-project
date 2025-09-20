package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.product.Supplier;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void saveSupplier(Supplier supplier) {
        supplierRepository.save(supplier);

        for(ProductBatch batch : supplier.getProductBatches()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(batch.getProduct().getId()));
        }
    }

}
