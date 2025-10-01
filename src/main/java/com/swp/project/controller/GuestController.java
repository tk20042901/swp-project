package com.swp.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.swp.project.dto.RegisterDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    @Value("${recaptcha.site-key}")
    private String recaptchaSite;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/guest/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/guest/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("siteKey", recaptchaSite);
            return "/pages/guest/register";
        }
        try {
            customerService.register(registerDto);
            redirectAttributes.addFlashAttribute("email", registerDto.getEmail());
            return "redirect:/verify-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@ModelAttribute("email") String email,
                                    Model model) {
        if(email == null || email.isEmpty()){
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        return "/pages/guest/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            Model model) {
        try {
            customerService.verifyOtp(email, otp);
            return "redirect:/login?register_success";
        } catch (RuntimeException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "pages/guest/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/guest/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            customerService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("success",
                    "Mật khẩu mới vừa được gửi tới " + email);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot-password";
    }


    @GetMapping({ "/" })
    public String getHomepage(
        @RequestParam(defaultValue = "0") Long categoryId,
        Model model) {

        List<Category> categories = categoryService.getAllCategories();
        Page<ViewProductDto> productsByCategory = productService.getViewProductsByCategoryWithPagingAndSorting(categoryId,0,CAROUSEL_SIZE,"default");
        
        // Static sections (newest, most sold, premium)
        Page<ViewProductDto> newestProducts = productService.getViewProductsByCategoryWithPagingAndSorting(0L, 0, CAROUSEL_SIZE, "newest");
        Page<ViewProductDto> mostSoldProducts = productService.getViewProductsByCategoryWithPagingAndSorting(0L, 0, CAROUSEL_SIZE, "best-seller");

        model.addAttribute("productByCategory", productsByCategory);
        model.addAttribute("newestProducts", newestProducts);
        model.addAttribute("mostSoldProducts", mostSoldProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("url", "/");
        model.addAttribute("Title", "Trang danh sách sản phẩm");
        model.addAttribute("showSearchBar", true);
        return "pages/guest/homepage";
    }

    @GetMapping("/search-product")
    public String searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "default") String sortBy,
            Model model) {
        Page<ViewProductDto> products = productService.searchViewProductDto(keyword,categoryId,page,size,sortBy);      
        List<Category> categories = categoryService.getUniqueCategoriesBaseOnPageOfProduct(productService.searchViewProductDto(keyword,0L,page,size,"default").getContent());
        model.addAttribute("viewProductDto", products);
        model.addAttribute("totalElement", products.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("categories", categories);
        model.addAttribute("Title", "Trang tìm kiếm");
        model.addAttribute("url", "/search-product");
        model.addAttribute("keyword", keyword);
        model.addAttribute("hadKeyword", true);
        model.addAttribute("showSearchBar", true);
        return "pages/guest/product-category-sorting";
    }

    @GetMapping("/product-category-sorting")
    public String getAllProductsWithSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(required = false) String sortBy,
            Model model) {

        // Add categories for the dropdown filter
        List<Category> categories = categoryService.getAllCategories();
        Page<ViewProductDto> productsPage = productService.getViewProductsByCategoryWithPagingAndSorting(categoryId, page, size, sortBy);
        
        model.addAttribute("viewProductDto", productsPage);
        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("categories", categories);
        model.addAttribute("Title", "Trang danh sách sản phẩm");
        model.addAttribute("url", "/product-category-sorting");
        model.addAttribute("showSearchBar", true);
        return "pages/guest/product-category-sorting";
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
        try {
            customerService.addShoppingCartItem(principal, productId, quantity);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/product/" + productId;
        }
        redirectAttributes.addFlashAttribute("msg", "Thêm sản phẩm vào giỏ hàng thành công");
        return "redirect:/product/" + productId;
    }
}
