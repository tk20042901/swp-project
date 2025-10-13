package com.swp.project.controller;

import com.swp.project.dto.CreateProductDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.dto.UpdateProductDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ImageService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final OrderStatusService orderStatusService;
    private final OrderService orderService;
    private final ProductService productService;
    private final ProductUnitService unitService;
    private final CategoryService categoryService;
    private final SellerRequestService sellerRequestService;
    private final ImageService imageService;

    @GetMapping("")
    public String sellerMain() {
        return "pages/seller/index";
    }

    @GetMapping("/all-orders")
    public String sellerProducts(@Valid @ModelAttribute SellerSearchOrderDto sellerSearchOrderDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("orders", orderService.getAllOrder());
            model.addAttribute("orderStatus", orderStatusService.getAllStatus());
            model.addAttribute("sellerSearchOrderDto", sellerSearchOrderDto);
            return "pages/seller/order/all-orders";
        }

        if (sellerSearchOrderDto.isEmpty()) {
            model.addAttribute("orders", orderService.getAllOrder());
        } else {
            Page<Order> orders = orderService.searchOrder(sellerSearchOrderDto);
            int totalPages = orders.getTotalPages();
            if (totalPages > 0 && Integer.parseInt(sellerSearchOrderDto.getGoToPage()) > totalPages) {
                bindingResult.rejectValue("goToPage", "invalid.range",
                        "Trang phải trong khoảng 1 đến " + totalPages);
                sellerSearchOrderDto.setGoToPage("1");
                orders = orderService.searchOrder(sellerSearchOrderDto);
            }
            model.addAttribute("orders", orders);
        }
        model.addAttribute("orderStatus", orderStatusService.getAllStatus());
        model.addAttribute("sellerSearchOrderDto", sellerSearchOrderDto);
        return "pages/seller/order/all-orders";
    }

    @GetMapping("/order-detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        if (orderService.isOrderItemQuantityMoreThanAvailable(orderId) &&
                orderStatusService.isPendingConfirmationStatus(orderService.getOrderById(orderId))) {
            model.addAttribute("warning",
                    "Cảnh báo: Một số sản phẩm trong đơn hàng này có số lượng lớn hơn số lượng hiện có trong kho.");
        }
        model.addAttribute("orderStatusService", orderStatusService);
        model.addAttribute("order", orderService.getOrderById(orderId));
        return "pages/seller/order/order-detail";
    }

    @PostMapping("/update-pending-order-status")
    public String updatePendingOrderStatus(@RequestParam Long orderId,
            @RequestParam String action,
            RedirectAttributes redirectAttributes) {
        if (action.equals("accept")) {
            if (orderService.isOrderItemQuantityMoreThanAvailable(orderId)) {
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi: Một số sản phẩm trong đơn hàng vừa chấp nhận có số lượng lớn hơn số lượng hiện có trong kho. Tác vụ bị hủy.");
            } else {
                orderService.doWhenOrderConfirmed(orderService.getOrderById(orderId));
                redirectAttributes.addFlashAttribute("msg", "Chấp nhận đơn hàng thành công");
            }
        } else if (action.equals("reject")) {
            orderService.setOrderStatus(orderId, orderStatusService.getCancelledStatus());
            redirectAttributes.addFlashAttribute("msg",
                    "Từ chối đơn hàng thành công");
        }
        return "redirect:/seller/all-orders";
    }

    @PostMapping("/update-processing-order-status")
    public String updateProcessingOrderStatus(@RequestParam Long orderId,
            RedirectAttributes redirectAttributes) {
        orderService.markOrderStatusAsShipping(orderService.getOrderById(orderId));
        redirectAttributes.addFlashAttribute("msg",
                "Cập nhật trạng thái đơn hàng thành Đang giao hàng thành công.\n" +
                        "Hệ thống đã tự động phân công Shipper cho đơn hàng.");
        return "redirect:/seller/all-orders";
    }

    @GetMapping("/all-products")
    public String getAllProductList(@RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProductForSeller(name, enabled, pageable);
        model.addAttribute("products", products);
        model.addAttribute("name", name);
        model.addAttribute("enabled", enabled);
        return "pages/seller/product/all-products";
    }

    @GetMapping("/product/product-detail/{productId}")
    public String getProductDetail(@PathVariable("productId") Long id, Model model) {
        Product product = productService.getProductById(id);
        int availableQuantity = productService.getAvailableQuantity(id);
        model.addAttribute("availableQuantity", availableQuantity);
        model.addAttribute("product", product);
        return "pages/seller/product/product-detail";
    }

    @GetMapping("/statistic-report/overview")
    public String getOverviewReport(Model model) {
        model.addAttribute("unitSold", orderService.getUnitSold());
        model.addAttribute("totalCanceledOrder", orderService.getTotalCancelledOrders());
        model.addAttribute("nearlySoldOutProducts", orderService.getNearlySoldOutProduct());
        model.addAttribute("nearlyExpiredProducts", orderService.getNearlyExpiredProduct());
        return "pages/seller/statistic-report/overview";
    }

    @GetMapping("/seller-update-product/{id}")
    public String showUpdateProductForm(
            @PathVariable Long id,
            Model model) {
        Product product = productService.getProductById(id);
        UpdateProductDto dto = UpdateProductDto
                .builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .unit(product.getUnit())
                .enabled(product.isEnabled())
                .categories(product.getCategories().stream().map(Category::getId).toList())
                .mainImage(product.getMain_image_url())
                .firstSubImage(product.getSub_images().get(0).getSub_image_url())
                .secondSubImage(product.getSub_images().get(1).getSub_image_url())
                .thirdSubImage(product.getSub_images().get(2).getSub_image_url())
                .build();
        model.addAttribute("units", unitService.getAllUnits());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("selectedProduct", product);
        model.addAttribute("updateProductDto", dto);
        return "pages/seller/product/update-product";
    }

    @PostMapping("/seller-update-product")
    public String handleUpdateProduct(
            @Valid @ModelAttribute UpdateProductDto updateProductDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            @RequestParam MultipartFile imageFile,
            @RequestParam MultipartFile firstImage,
            @RequestParam MultipartFile secondImage,
            @RequestParam MultipartFile thirdImage) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        try {
            List<Category> categories = new ArrayList<>();
            String productName = updateProductDto.getName().trim();
            for (Long catId : updateProductDto.getCategories()) {
                categories.add(categoryService.getCategoryById(catId));
            }
            if (productService.checkUniqueProductName(updateProductDto.getName())) {
                throw new Exception("Tên sản phẩm đã tồn tại");
            }
            updateProductDto.setMainImage(imageService.saveImageToTemporaryFile(productName, imageFile));
            updateProductDto
                    .setFirstSubImage(imageService.saveImageToTemporaryFile(productName + "-1", firstImage));
            updateProductDto.setSecondSubImage(
                    imageService.saveImageToTemporaryFile(productName + "-2", secondImage));
            updateProductDto
                    .setThirdSubImage(imageService.saveImageToTemporaryFile(productName + "-3", thirdImage));

            UpdateProductDto dto = UpdateProductDto
                    .builder().id(updateProductDto.getId()).build();
            sellerRequestService.saveUpdateRequest(
                    updateProductDto,
                    dto,
                    principal.getName());

            redirectAttributes.addFlashAttribute("msg", "Yêu cầu cập nhật sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        return "redirect:/seller";
    }

    @GetMapping("/seller-create-product")
    public String showCreateProductForm(Model model) {
        CreateProductDto newProduct = new CreateProductDto();
        model.addAttribute("productDto", newProduct);
        model.addAttribute("units", unitService.getAllUnits());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "pages/seller/product/create-product";
    }

    @PostMapping("/seller-create-product")
    public String handleCreateProduct(
            @Valid @ModelAttribute CreateProductDto productDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam MultipartFile firstImage,
            @RequestParam MultipartFile secondImage,
            @RequestParam MultipartFile thirdImage,
            @RequestParam List<Long> categoryIds,   
            Model model) {

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            model.addAttribute("error", "Vui lòng kiểm tra các trường thông tin");
            return "redirect:/seller/seller-create-product";
        }
        try {
            if (productService.checkUniqueProductName(productDto.getName())) {
                throw new Exception("Tên sản phẩm đã tồn tại");
            }
            List<Category> categories = new ArrayList<>();
            for (Long catId : categoryIds) {
                categories.add(categoryService.getCategoryById(catId));
            }
            productDto.setCategories(categories);
            String productName = productDto.getName().trim();
            productDto.setMain_image_url(imageService.saveImageToTemporaryFile(productName, imageFile));
            productDto.setFirstSubImage(imageService.saveImageToTemporaryFile(productName + "-1", firstImage));
            productDto.setSecondSubImage(imageService.saveImageToTemporaryFile(productName + "-2", secondImage));
            productDto.setThirdSubImage(imageService.saveImageToTemporaryFile(productName + "-3", thirdImage));
            sellerRequestService.saveAddRequest(productDto, productName);
            redirectAttributes.addFlashAttribute("msg", "Yêu cầu tạo sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller";
        }
        return "redirect:/seller";
    }

}
