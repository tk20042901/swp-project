package com.swp.project.listener;

import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.service.CustomerAiService;
import com.swp.project.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductRelatedUpdateEventListener {

    private final CustomerAiService customerAiService;
    private final ProductService productService;

    @EventListener
    public void onProductRelatedUpdateEvent(ProductRelatedUpdateEvent event) {
        customerAiService.saveProductToVectorStore(productService.getProductById(event.productId()));
    }
}
