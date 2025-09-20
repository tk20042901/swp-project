package com.swp.project.service.product;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void saveCategory(Category category) {
        categoryRepository.save(category);

        for (Product product : category.getProducts()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product.getId()));
        }
    }
}
