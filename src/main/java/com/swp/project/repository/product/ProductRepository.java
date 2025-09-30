package com.swp.project.repository.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    boolean existsByName(String name);

    Product findByName(String productName);

    List<Product> getByName(String name);
    List<Product> findDistinctByCategoriesInAndIdNot(List<Category> categories, Long id, PageRequest of);

}
