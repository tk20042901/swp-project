package com.swp.project.controller;

import com.swp.project.dto.RegisterDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Product;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final static int PAGE_SIZE = 10;

    @Value("${recaptcha.site-key}")
    private String recaptchaSite;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("siteKey", recaptchaSite);
            return "/pages/auth/register";
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
        return "/pages/auth/verify-otp";
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
            return "pages/auth/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/forgot-password";
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

    @GetMapping({"/"})
    public String getHomepage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Model model) {
        Page<Product> productsPage;
        // Sửa lỗi nếu tìm kiếm và lọc category, không hiển thị đúng danh mục
        // Lưu trang tìm kiếm để lấy danh mục
        Page<Product> productSearchPage = null;
        if (keyword != null && !keyword.isEmpty() && categoryId != null && categoryId != 0) {
            productSearchPage = productService.searchProductsWithPaging(keyword, page, size);
            productsPage = productService.searchProductsThenSortByCategoryWithPaging(keyword, categoryId, page, size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
        } else if (keyword != null && !keyword.isEmpty()) {
            productsPage = productService.searchProductsWithPaging(keyword, page, size);
            productSearchPage = productsPage;
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", 0);
        } else if (categoryId != null && categoryId != 0) {
            productsPage = productService.getProductsByCategoryWithPaging(categoryId, page, size);
            model.addAttribute("categoryId", categoryId);
        } else {
            productsPage = productService.getProductsWithPaging(page, size);
            model.addAttribute("categoryId", 0);
        }
        model.addAttribute("viewProductDto", mapProductToViewProductDto(productsPage));
        //Nếu search thì lấy danh mục từ trang search, không thì lấy từ trang products bình thường
        if (productSearchPage != null) {
            model.addAttribute("categories", categoryService.getUniqueCategoriesBaseOnPageOfProduct(productSearchPage)); 
        }else{
            model.addAttribute("categories", categoryService.getUniqueCategoriesBaseOnPageOfProduct(productsPage)); 
        }
        return "pages/customer/index";
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