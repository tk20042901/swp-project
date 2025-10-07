package com.swp.project.listener.event;

import com.swp.project.entity.GeminiStorable;

/**
 * Generic event for vector store updates.
 * This event can be used with any entity that implements GeminiStorable.
 *
 * @param <T> the entity type that extends GeminiStorable
 */
public record GeminiUpdateEvent<T extends GeminiStorable>(T entity, UpdateType type) {
    public enum UpdateType {
        CREATE,  // Entity was created
        UPDATE,  // Entity was modified
        DELETE   // Entity was removed
    }
}