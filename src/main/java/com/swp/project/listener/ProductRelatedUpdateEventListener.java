package com.swp.project.listener;

import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.service.CustomerAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductRelatedUpdateEventListener {

    private final CustomerAiService customerAiService;

    @EventListener
    public void onProductRelatedUpdateEvent(ProductRelatedUpdateEvent event) {
        customerAiService.saveProductToVectorStore(event.product());
    }

}