package com.swp.project.service.product;

import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.repository.product.ProductBatchRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductBatchRepository productBatchRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;

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

    public int getAvailableQuantity(Long productId) {
        productBatchRepository.getByProduct_Id(productId);
        return productBatchRepository.getByProduct_Id(productId)
                .stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();
    }

    public ShoppingCartItem getAllShoppingCartItemByCustomerIdAndProductId(String email, Long productId) {
        return shoppingCartItemRepository.findByCustomer_EmailAndProduct_Id(email, productId);
    }
}
