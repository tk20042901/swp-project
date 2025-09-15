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

import java.util.Random;

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

        if (products.isEmpty() || suppliers.isEmpty()) return;

        Random random = new Random();

        for (Product product : products) {
            // tạo 2–5 batch cho mỗi product
            int batchCount = 2 + random.nextInt(4); // 2,3,4,5 batch
            for (int i = 0; i < batchCount; i++) {
                Supplier supplier = suppliers.get(random.nextInt(suppliers.size()));

                ProductBatch batch = ProductBatch.builder()
                        .product(product)
                        .suppliers(supplier)
                        .seller(sellerRepository.findByEmail("seller1@shop.com"))
                        .quantity(50 + random.nextInt(101)) // số lượng 50–150
                        .expiredDate(Instant.now().plusSeconds(86400L * (30 + random.nextInt(31)))) // 30–60 ngày
                        .build();

                productBatchRepository.save(batch);
            }
        }
    }
}
