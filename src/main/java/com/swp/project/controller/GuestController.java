package com.swp.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.swp.project.dto.AiMessageDto;
import com.swp.project.service.CustomerAiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
public class GuestController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomerAiService customerAiService;
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
        model.addAttribute("showSearchBar", true);
        return "pages/guest/homepage";
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
        model.addAttribute("showSearchBar", true);
        return "pages/guest/search-result";
    }

    private Page<ViewProductDto> mapProductToViewProductDto(Page<Product> products) {
        return products.map(product -> ViewProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice().doubleValue())
                .mainImageUrl(product.getMain_image_url())
                .build());
    }

    @GetMapping("/product/{id}")
    public String getProductDetail(
            @PathVariable(name = "id") Long id,
            Model model) {
        Product product = productService.getProductById(id);
        List<SubImage> subImages = product.getSub_images();
        model.addAttribute("product", product);
        model.addAttribute("subImages", subImages);
        return "pages/guest/product-details";
    }

    @GetMapping("/ai")
    public String ask(Model model, HttpSession session) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        session.removeAttribute("conversation");
        session.setAttribute("conversation", new ArrayList<AiMessageDto>());
        return "pages/guest/ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId,
                      @RequestParam String q,
                      @RequestParam MultipartFile image,
                      HttpSession session,
                      Model model) {
        List<AiMessageDto> conversation = (List<AiMessageDto>) session.getAttribute("conversation");
        try {
            customerAiService.ask(conversationId, q, image, conversation);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("conversationId", conversationId);
        session.setAttribute("conversation", conversation);
        model.addAttribute("conversation", conversation);
        return "pages/guest/ai";
    }
}
