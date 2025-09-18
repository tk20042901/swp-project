package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public int getAvailableQuantity(Product product) {
        int total = 0;
        for (ProductBatch batch : product.getProductBatches()) {
            total += batch.getQuantity();
        }
        return total;
    }
}
