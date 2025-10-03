package com.swp.project.listener.event;

import com.swp.project.listener.VectorStorable;

/**
 * Generic event for vector store updates.
 * This event can be used with any entity that implements VectorStorable.
 * 
 * @param <T> the entity type that extends VectorStorable
 */
public record VectorUpdateEvent<T extends VectorStorable>(T entity, UpdateType type) {
    
    /**
     * Types of operations that can trigger vector store updates.
     */
    public enum UpdateType {
        CREATE,  // Entity was created
        UPDATE,  // Entity was modified
        DELETE   // Entity was removed
    }
}