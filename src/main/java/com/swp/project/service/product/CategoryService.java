package com.swp.project.service.product;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy danh sách các danh mục duy nhất dựa trên trang sản phẩm đã cho.
     *
     * @param productsPage trang sản phẩm
     * @return danh sách các danh mục duy nhất liên kết với các sản phẩm trong trang
     */
    public List<Category> getUniqueCategoriesBaseOnPageOfProduct(Page<Product> productsPage) {
        List<Category> categories = new ArrayList<>();
        for (Product product : productsPage.getContent()) {
            for (Category category : product.getCategories()) {
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
        }
        return categories;
    }
    private final ApplicationEventPublisher eventPublisher;

    public void saveCategory(Category category) {
        categoryRepository.save(category);

        for (Product product : category.getProducts()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product.getId()));
        }
    }
}
