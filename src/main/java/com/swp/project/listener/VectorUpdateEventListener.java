package com.swp.project.listener;

import com.swp.project.listener.event.VectorUpdateEvent;
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
public class VectorUpdateEventListener {

    private final CustomerAiService customerAiService;

    /**
     * Handles vector update cho các entity implementing VectorStorable.
     * 
     * @param event the vector update event containing entity and operation type
     */
    @EventListener
    public void onVectorUpdateEvent(VectorUpdateEvent<?> event) {
        try {
            switch (event.type()) {
                case CREATE, UPDATE -> {
                    customerAiService.saveEntityToVectorStore(event.entity());
                }
                case DELETE -> {
                    customerAiService.removeEntityFromVectorStore(event.entity().getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to update vector store for {} with ID: {}. Error: {}", 
                     event.entity().getClass().getSimpleName(), 
                     event.entity().getId(), 
                     e.getMessage());
        }
    }
}