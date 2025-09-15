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
        String[] suppliers = {"Công ty Nông sản Thanh Hóa",
                "Công ty cổ phần 36",
                "Công ty Nông sản Raumania",
                "Công ty FPFruit",
                "Công ty Hiệu Nhẫn Giả",
                "Công ty Trái Cây Việt Nam",
                "Công ty Nông sản Sài Gòn",
                "Công ty Đầu tư Nông sản Hà Nội",
                "Công ty Thực phẩm Xanh",
                "Công ty Nông sản Việt Pháp",
                "Công ty Trái Cây Nam Bộ",
                "Công ty Green Farm",
                "Công ty Fresh Fruits",
                "Công ty Organic Việt",
                "Công ty Trái Cây Toàn Cầu",
                "Công ty Trái Cây An Toàn",
                "Công ty Nông sản Hữu Cơ",
                "Công ty Fresh Farm Việt",
                "Công ty Trái Cây Miền Trung",
                "Công ty Organic Farm",
                "Công ty Nông sản Cao Nguyên",
                "Công ty Trái Cây Sạch",
                "Công ty Nông sản Toàn Cầu",
                "Công ty Thực Phẩm Tươi",
                "Công ty Green Fruits",
                "Công ty Tropical Fruits",
                "Công ty EcoFarm Việt Nam",
                "Công ty Việt Fruit",
                "Công ty Sunshine Farm",
                "Công ty Fresh Harvest"};
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
