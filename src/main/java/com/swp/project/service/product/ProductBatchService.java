package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.product.Supplier;
import com.swp.project.repository.product.ProductBatchRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.product.SupplierRepository;
import com.swp.project.repository.user.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service

public class ProductBatchService {


    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SellerRepository sellerRepository;
    @Transactional
    public void initProductBatches() {
        List<Product> products = productRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();

        if (products.isEmpty() || suppliers.isEmpty()) {
            return;
        }

        // Map tên sản phẩm -> tên supplier
        Map<String, String> supplyMap = Map.of(
                "Táo đỏ", "Công ty Nông sản Thanh Hóa",
                "Cam vàng", "Công ty FPFruit",
                "Chuối sứ", "Công ty cổ phần 36",
                "Dâu tây", "Công ty Nông sản Raumania",
                "Nho tím", "Công ty Hiệu Nhẫn Giả",
                "Xoài cát", "Công ty Nông sản Thanh Hóa"
        );

        for (Product product : products) {
            String supplierName = supplyMap.get(product.getName());
            if (supplierName == null) continue; // Nếu sản phẩm chưa map thì bỏ qua

            Supplier supplier = suppliers.stream()
                    .filter(s -> s.getName().equals(supplierName))
                    .findFirst()
                    .orElse(null);

            if (supplier == null) continue; // Nếu không tìm thấy supplier thì bỏ qua

            ProductBatch batch = ProductBatch.builder()
                    .product(product)
                    .suppliers(supplier)
                    .seller(sellerRepository.findByEmail("seller1@shop.com"))
                    .quantity(100)
                    .expiredDate(Instant.now().plusSeconds(86400L * 30)) // 30 ngày
                    .build();

            productBatchRepository.save(batch);
        }
    }
}
