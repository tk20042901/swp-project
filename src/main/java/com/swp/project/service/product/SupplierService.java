package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.product.Supplier;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Supplier getSupplierById(Long id){
        return supplierRepository.findById(id).orElse(null);
    }

    public void addSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void updateSupplier(Supplier supplier) {
        supplierRepository.save(supplier);

        List<Product> relatedProducts = getSupplierById(supplier.getId()).getProductBatches()
                .stream()
                .map(ProductBatch::getProduct)
                .distinct()
                .toList();
        for(Product product : relatedProducts) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product.getId()));
        }
    }

}
