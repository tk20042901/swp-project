package com.swp.project.repository.product;
import com.swp.project.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

import com.swp.project.entity.product.Product;


@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    boolean existsByName(String name);

    @Query("SELECT DISTINCT c FROM Product p JOIN p.categories c WHERE p.id IN :productIds")
    List<Category> findDistinctCategoriesByProductIds(@Param("productIds") List<Long> productIds);

}
