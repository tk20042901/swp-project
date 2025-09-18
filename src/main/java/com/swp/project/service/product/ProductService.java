package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.repository.product.ProductBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductBatchRepository productBatchRepository;


    @Transactional
    public void pickProductInProductBatch(Long productId, int quantity){
        List<ProductBatch> productBatches = productBatchRepository.getByProduct_Id(productId);
        productBatches.sort(Comparator.comparing(ProductBatch::getExpiredDate)
                .thenComparingInt(ProductBatch::getQuantity));
        for (ProductBatch productBatch : productBatches) {
            if (quantity <= 0) break;
            if (productBatch.getQuantity() >= quantity) {
                productBatch.setQuantity(productBatch.getQuantity() - quantity);
                quantity = 0;
            } else {
                quantity -= productBatch.getQuantity();
                productBatch.setQuantity(0);
            }
            productBatchRepository.save(productBatch);
        }
    }
}
