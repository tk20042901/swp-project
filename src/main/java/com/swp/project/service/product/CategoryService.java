package com.swp.project.service.product;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.listener.event.GeminiUpdateEvent;
import com.swp.project.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);

        // Publish vector update event for the category itself
        eventPublisher.publishEvent(new GeminiUpdateEvent<>(savedCategory, GeminiUpdateEvent.UpdateType.UPDATE));

        // Also update all products in this category since category info affects product vector content
        for (Product product : getCategoryById(category.getId()).getProducts()) {
            eventPublisher.publishEvent(new GeminiUpdateEvent<>(product, GeminiUpdateEvent.UpdateType.UPDATE));
        }
    }

    public List<Category> getUniqueCategoriesBaseOnPageOfProduct(List<ViewProductDto> content) {
        List<Long> ids = content.stream().map(ViewProductDto::getId).toList();
        return categoryRepository.findDistinctCategoriesByProductIds(ids);
    }
}
