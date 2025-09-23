package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.ProductUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductUnit getProductUnitById(Long id){
        return productUnitRepository.findById(id).orElse(null);
    }

    public void addProductUnit(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);
    }

    public void updateProductUnit(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);

        for(Product product : productUnit.getProducts()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product.getId()));
        }
    }
}
