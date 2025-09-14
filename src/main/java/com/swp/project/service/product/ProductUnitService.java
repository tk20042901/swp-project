package com.swp.project.service.product;

import com.swp.project.entity.product.ProductUnit;
import com.swp.project.repository.product.ProductUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;

    @Transactional
    public void initProductUnit() {
        String[] units = {"Kg", "Quả", "Hộp"};
        for (String unit : units) {
            if (!productUnitRepository.existsByName(unit)) {
                productUnitRepository.save(ProductUnit.builder()
                        .name(unit)
                        .build()
                );
            }
        }
    }

}
