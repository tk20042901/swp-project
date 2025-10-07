package com.swp.project.repository.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    Product findByName(String productName);

    List<Product> getByName(String name);

    List<Product> findDistinctByCategoriesInAndIdNot(List<Category> categories, Long id, Pageable pageable);

    @Query("""
        SELECT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    Page<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    List<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    Page<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    List<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled
    );

    @Query(""" 
        SELECT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        JOIN p.categories c 
        WHERE p.enabled = :enabled
        AND c.id = :categoryId
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    Page<ViewProductDto> findViewProductDtoByProductNameAndCategoryIdAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @Query(""" 
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    Page<ViewProductDto> findViewProductDtoByProductNameAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    List<Product> findAllByEnabled(boolean enabled);

    /**
     * Optimized query to get homepage products in batches
     * This method returns different product sets based on sort criteria to reduce database calls
     */
    @Query(value = """
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'newest' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.id DESC 
         LIMIT :limit)
        UNION ALL
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'best-seller' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.sold_quantity DESC 
         LIMIT :limit)
        UNION ALL
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'default' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.id 
         LIMIT :limit)
        """, nativeQuery = true)
    List<Object[]> findHomepageProductsBatch(@Param("limit") int limit);

    Page<Product> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByEnabled(Boolean enabled, Pageable pageable);

    Product findFirstByEnabledOrderByIdAsc(boolean enabled);
}
