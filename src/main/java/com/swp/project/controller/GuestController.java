package com.swp.project.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Product;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Controller
public class GuestController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final static int PAGE_SIZE = 9;
    @GetMapping({"/"})
    public String getHomepage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        Page<Product> productsPage;
        if (categoryId != null && categoryId != 0) {
            productsPage = productService.getProductsByCategoryWithPaging(categoryId, page, size);
            model.addAttribute("categoryId", categoryId);
        } else {
            productsPage = productService.getProductsWithPaging(page, size);
            model.addAttribute("categoryId", 0);
        }
        model.addAttribute("viewProductDto", mapProductToViewProductDto(productsPage));
        model.addAttribute("categories", categoryService.getAllCategories()); 
        model.addAttribute("url", "/");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        return "fragments/homepage";
    }

    @GetMapping("/search-product")
    public String searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Model model) {
        Page<Product> productsPage;
        if (keyword != null && !keyword.isEmpty() && categoryId != null && categoryId != 0) {
            productsPage = productService.searchProductsThenSortByCategoryWithPaging(keyword, categoryId, page, size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
        } else{
            productsPage = productService.searchProductsWithPaging(keyword, page, size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", 0);
        }
        model.addAttribute("viewProductDto", mapProductToViewProductDto(productsPage));
        model.addAttribute("categories", categoryService.getUniqueCategoriesBaseOnPageOfProduct(productService.searchProductsWithPaging(keyword, page, size))); 

        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("url", "/search-product" + (keyword != null ? "?keyword=" + keyword : ""));
        return "fragments/search-result";
    }

    private Page<ViewProductDto> mapProductToViewProductDto(Page<Product> products) {
        return products.map(product -> ViewProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice().doubleValue())
                .mainImageUrl(product.getMain_image_url())
                .build());
    }
    
}
