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
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    Page<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    List<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled
    );

    @Query("""
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    Page<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    List<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled
    );

    @Query(""" 
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
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
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p 
        JOIN p.categories c 
        WHERE p.enabled = :enabled
        AND c.id = :categoryId
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    List<ViewProductDto> findViewProductDtoByProductNameAndCategoryIdAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        @Param("categoryId") Long categoryId
    );

    @Query(""" 
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p 
        WHERE p.enabled = :enabled
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    Page<ViewProductDto> findViewProductDtoByProductNameAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query(""" 
        SELECT DISTINCT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url) 
        FROM Product p 
        WHERE p.enabled = :enabled
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    List<ViewProductDto> findViewProductDtoByProductNameAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled
    );

    List<Product> findAllByEnabled(boolean enabled);
}
