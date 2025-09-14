package com.swp.project.service.product;

import com.swp.project.entity.product.Supplier;
import com.swp.project.repository.product.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
@Transactional
    public void initSupplier() {
        String[] suppliers = {"Công ty Nông sản Thanh Hóa", "Công ty cổ phần 36", "Công ty Nông sản Raumania", "Công ty FPFruit", "Công ty Hiệu Nhẫn Giả"};
        for (String supplier : suppliers) {
            if (!supplierRepository.existsByName(supplier)) {
                supplierRepository.save(Supplier.builder()
                        .name(supplier)
                        .build()
                );
            }

        }
    }
}
