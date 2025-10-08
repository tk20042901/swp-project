package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.listener.event.GeminiUpdateProductEvent;
import com.swp.project.repository.product.ProductUnitRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
    public List<ProductUnit> getAllUnits(){
        return productUnitRepository.findAll();
    }

    public void addProductUnit(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);
    }

    public void updateProductUnit(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);
        for(Product product : getProductUnitById(productUnit.getId()).getProducts()) {
             eventPublisher.publishEvent(new GeminiUpdateProductEvent(product));
        }
    }
}
