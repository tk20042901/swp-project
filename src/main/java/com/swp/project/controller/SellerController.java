package com.swp.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.swp.project.entity.product.SubImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.swp.project.dto.CreateProductDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.dto.UpdateProductDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ImageService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public String index() {
        return "pages/seller/index";
    }

    @GetMapping("/all-orders")
    public String allOrdersList(@Valid @ModelAttribute SellerSearchOrderDto sellerSearchOrderDto,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("orders", orderService.getAllOrder());
            model.addAttribute("orderStatusService", orderStatusService);
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
        model.addAttribute("orderStatusService", orderStatusService);
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
        double availableQuantity = productService.getAvailableQuantity(id);
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
            Principal principal) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        try {
            Product oldProduct = productService.getProductById(updateProductDto.getId());
            List<Category> categories = new ArrayList<>();
            for (Long catId : updateProductDto.getCategories()) {
                categories.add(categoryService.getCategoryById(catId));
            }
            if (!updateProductDto.getName().equals(oldProduct.getName()) && productService.checkUniqueProductName(updateProductDto.getName())) {
                throw new Exception("Tên sản phẩm đã tồn tại");
            }
            Product updateProduct = Product.builder()
                    .id(updateProductDto.getId())
                    .name(updateProductDto.getName())
                    .description(updateProductDto.getDescription())
                    .price(updateProductDto.getPrice())
                    .unit(updateProductDto.getUnit())
                    .enabled(updateProductDto.getEnabled())
                    .main_image_url(oldProduct.getMain_image_url())
                    .sub_images(oldProduct.getSub_images())
                    .categories(categories)
                    .build();
            sellerRequestService.saveUpdateRequest(oldProduct, updateProduct, principal.getName());
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
            @RequestParam List<Long> categoryIds,
            @RequestParam MultipartFile imageFile,
            @RequestParam MultipartFile[] subImageFiles,   
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
            String fileName = ProductService.toSlugName(productDto.getName());
            Product product = Product.builder()
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .price(productDto.getPrice())
                    .unit(productDto.getUnit())
                    .enabled(productDto.isEnabled())
                    .categories(productDto.getCategories())
                    .main_image_url(imageService.saveTemporaryImage(imageFile, fileName, "1.jpg"))
                    .build();
            List<SubImage> subImages = new ArrayList<>();
            for (int i = 0; i < subImageFiles.length; i++) {
                MultipartFile subImageFile = subImageFiles[i];
                    String subImagePath = imageService.saveTemporaryImage(subImageFile, fileName, (i + 2) + ".jpg");
                    SubImage subImage = SubImage.builder()
                            .product(product)
                            .sub_image_url(subImagePath)
                            .build();
                    subImages.add(subImage);
            }
            product.setSub_images(subImages);
            sellerRequestService.saveAddRequest(product, principal.getName());
            redirectAttributes.addFlashAttribute("msg", "Yêu cầu tạo sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller";
        }
        return "redirect:/seller";
    }

    @GetMapping("/update-product-images/{productId}")
    public String showUpdateProductImagesForm(@PathVariable Long productId, Model model) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new Exception("Sản phẩm không tồn tại");
            }
            model.addAttribute("product", product);
            return "pages/seller/product/update-product-images";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/seller/all-products";
        }
    }

    @PostMapping("/update-product-images")
    public String handleUpdateProductImages(
            @RequestParam Long productId,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) MultipartFile[] subImages,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            Product oldProduct = productService.getProductById(productId);
            if (oldProduct == null) {
                throw new Exception("Sản phẩm không tồn tại");
            }

            Product updatedProduct = oldProduct.toBuilder().build();

            if (mainImage != null && !mainImage.isEmpty()) {
                if (!Objects.requireNonNull(mainImage.getContentType()).startsWith("image/")) {
                    throw new Exception("Tệp hình ảnh chính không đúng định dạng");
                }
                                String mainImagePath = imageService.saveTemporaryImage(mainImage, productId + "", "temp-1.jpg");
                updatedProduct.setMain_image_url(mainImagePath);
            }

            // Handle sub images update - filter out empty files and process valid ones
            if (subImages != null && subImages.length > 0) {
                List<MultipartFile> validSubImages = new ArrayList<>();
                
                // Filter out empty files
                for (MultipartFile subImage : subImages) {
                    if (subImage != null && !subImage.isEmpty()) {
                        validSubImages.add(subImage);
                    }
                }

                if (!validSubImages.isEmpty()) {
                    List<SubImage> newSubImages = new ArrayList<>();                    
                    for (int i = 0; i < validSubImages.size(); i++) {
                        MultipartFile subImage = validSubImages.get(i);

                        if (!Objects.requireNonNull(subImage.getContentType()).startsWith("image/")) {
                            throw new Exception("Tệp hình ảnh phụ " + (i + 1) + " không đúng định dạng");
                        }

                        String subImagePath = imageService.saveTemporaryImage(subImage, productId + "","temp-" + (i + 2) + ".jpg");
                        SubImage subImageEntity = SubImage.builder()
                                .product(updatedProduct)
                                .sub_image_url(subImagePath)
                                .build();
                        newSubImages.add(subImageEntity);
                    }
                    
                    updatedProduct.setSub_images(newSubImages);
                }
            }
            // Only proceed if at least one image was updated
            boolean hasMainImageUpdate = mainImage != null && !mainImage.isEmpty();
            boolean hasSubImagesUpdate = subImages != null && 
                                        java.util.Arrays.stream(subImages).anyMatch(img -> img != null && !img.isEmpty());
            
            if (!hasMainImageUpdate && !hasSubImagesUpdate) {
                throw new Exception("Vui lòng chọn ít nhất một hình ảnh để cập nhật");
            }

            // Submit update request
            sellerRequestService.saveUpdateRequest(oldProduct, updatedProduct, principal.getName());
            redirectAttributes.addFlashAttribute("msg", "Yêu cầu cập nhật hình ảnh sản phẩm đã được gửi đến quản lý");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/update-product-images/" + productId;
        }
        return "redirect:/seller/product/product-detail/" + productId;
    }

}
