package com.swp.project.listener;

import com.swp.project.entity.product.Product;
import com.swp.project.listener.event.GeminiUpdateCategoryEvent;
import com.swp.project.listener.event.GeminiUpdateProductEvent;
import com.swp.project.service.CustomerAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Generic event listener for vector store updates.
 * This listener handles VectorUpdateEvent for any entity implementing VectorStorable.
 * It automatically routes CREATE/UPDATE operations to save and DELETE operations to remove.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class GeminiUpdateEventListener {

    private final CustomerAiService customerAiService;

    @EventListener
    public void onVectorProductUpdateEvent(GeminiUpdateProductEvent event) {
        customerAiService.saveProductToVectorStore(event.product());
    }

    @EventListener
    public void onVectorCategoryUpdateEvent(GeminiUpdateCategoryEvent event) {
        customerAiService.saveCategoryToVectorStore(event.category());
    }
}