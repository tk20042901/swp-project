package com.swp.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp.project.dto.AiMessageDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.service.CustomerAiService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.CustomerService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class GuestController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomerAiService customerAiService;
    private final CustomerService customerService;
    private final static int PAGE_SIZE = 9;
    private final static int CAROUSEL_SIZE = 6;

    @GetMapping({ "/" })
    public String getHomepage(Model model) {
        // Load products for each category (first 6 products)
        Map<String, List<ViewProductDto>> productsByCategory = new HashMap<>();
        
        // All products (category "0")
        Page<Product> allProductsPage = productService.getProductsWithPaging(0, CAROUSEL_SIZE);
        productsByCategory.put("0", mapProductToViewProductDto(allProductsPage).getContent());
        
        // Each specific category
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
            Page<Product> categoryProductsPage = productService.getProductsByCategoryWithPaging(category.getId(), 0, CAROUSEL_SIZE);
            productsByCategory.put(category.getId().toString(), mapProductToViewProductDto(categoryProductsPage).getContent());
        }
        
        // Static sections (newest, most sold, premium)
        Page<Product> newestProducts = productService.getProductsByCategoryWithPagingAndSorting(0L, 0, CAROUSEL_SIZE, "newest");
        Page<Product> mostSoldProducts = productService.getProductsByCategoryWithPagingAndSorting(0L, 0, CAROUSEL_SIZE, "best-seller");
        Page<Product> premiumProducts = productService.getProductsByCategoryWithPagingAndSorting(0L, 0, CAROUSEL_SIZE, "price-desc");

        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("newestProducts", mapProductToViewProductDto(newestProducts));
        model.addAttribute("mostSoldProducts", mapProductToViewProductDto(mostSoldProducts));
        model.addAttribute("premiumProducts", mapProductToViewProductDto(premiumProducts));
        model.addAttribute("categories", categories);
        model.addAttribute("url", "/");
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
        } else {
            productsPage = productService.searchProductsWithPaging(keyword, page, size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", 0);
        }
        model.addAttribute("viewProductDto", mapProductToViewProductDto(productsPage));
        model.addAttribute("categories", categoryService
                .getUniqueCategoriesBaseOnPageOfProduct(productService.searchProductsWithPaging(keyword, page, size)));

        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("url", "/search-product" + (keyword != null ? "?keyword=" + keyword : ""));
        model.addAttribute("showSearchBar", true);
        return "pages/guest/search-result";
    }

    @GetMapping("/product-category-display/{categoryId}")
    public String getProductsByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @PathVariable Long categoryId,
            @RequestParam(required = false) String sortBy,
            Model model) {
        
        Page<Product> productsPage = productService.getProductsByCategoryWithPagingAndSorting(categoryId, page, size, sortBy);
        
        Page<ViewProductDto> dtoPage = mapProductToViewProductDto(productsPage);
        
        // Get category name for display
        Category category = categoryService.getCategoryById(categoryId);
        String categoryName = (category != null) ? category.getName() : "Tất cả sản phẩm";
        
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("viewProductDto", dtoPage);
        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("url", "/product-category-display/" + categoryId);
        model.addAttribute("showSearchBar", true);
        return "pages/guest/products-category";
    }

    @GetMapping("/all-product-sorting/{categoryId}")
    public String getAllProductsWithSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @PathVariable Long categoryId,
            @RequestParam(required = false) String sortBy,
            Model model) {
        
        // Default to all products if no category specified
        Long targetCategoryId = (categoryId != null) ? categoryId : 0L;
        
        // Use the same method that handles sorting before pagination
        Page<Product> productsPage = productService.getProductsByCategoryWithPagingAndSorting(targetCategoryId, page, size, sortBy);
        
        Page<ViewProductDto> dtoPage = mapProductToViewProductDto(productsPage);
        
        // Get category name for display
        Category category = categoryService.getCategoryById(targetCategoryId);
        String categoryName = (category != null) ? category.getName() : "Tất cả sản phẩm";
        
        model.addAttribute("categoryId", targetCategoryId);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("viewProductDto", dtoPage);
        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("url", "/all-product-sorting");
        model.addAttribute("showSearchBar", true);
        return "pages/guest/all-product-sorting";
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
            Model model,
            Principal principal) {
        Product product = productService.getProductById(id);
        List<SubImage> subImages = product.getSub_images();
        model.addAttribute("product", product);
        model.addAttribute("subImages", subImages);
        model.addAttribute("maxQuantity", productService.getAvailableQuantity(id));

        List<Product> relatedProducts = productService.getRelatedProducts(id, 6);
        model.addAttribute("relatedProducts", relatedProducts);

        int soldQuantity = productService.getSoldQuantity(id);
        model.addAttribute("soldQuantity", soldQuantity);

        List<Category> categories = product.getCategories();
        model.addAttribute("categories", categories);

        return "pages/guest/product";
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

    @PostMapping("/product/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/product/" + productId;
        } else {
            try {
                customerService.addShoppingCartItem(principal.getName(), productId, quantity);
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return "redirect:/product/" + productId;
            }
        }
        redirectAttributes.addFlashAttribute("msg", "Thêm sản phẩm vào giỏ hàng thành công");
        return "redirect:/product/" + productId;
    }
}
