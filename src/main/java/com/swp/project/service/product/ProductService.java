package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductBatchService productBatchService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void saveProduct(Product product) {
        productRepository.save(product);

        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product.getId()));
    }


    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void pickProductInProductBatch(Long productId, int quantity){
        List<ProductBatch> productBatches = productBatchService.getByProductId(productId);
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
            productBatchService.saveProductBatch(productBatch);
        }
    }

    public int getAvailableQuantity(Long productId) {
        return productBatchService.getByProductId(productId)
                .stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();
    }

    public ShoppingCartItem getAllShoppingCartItemByCustomerIdAndProductId(String email, Long productId) {
        return shoppingCartItemRepository.findByCustomer_EmailAndProduct_Id(email, productId);
    }
}
