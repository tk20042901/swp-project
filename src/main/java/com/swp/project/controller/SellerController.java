package com.swp.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import com.swp.project.entity.product.ProductUnit;
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
import com.swp.project.dto.CreateProductUnitDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.dto.UpdateProductDto;
import com.swp.project.dto.UpdateProductUnitDto;
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
    private final ProductUnitService productUnitService;

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
        return "pages/seller/statistic-report/overview";
    }
    @GetMapping("/statistic-report")
    public String getSellerReport(Model model) {
        model.addAttribute("unitSold", orderService.getUnitSold());
        model.addAttribute("totalCanceledOrder", orderService.getTotalCancelledOrders());
        model.addAttribute("nearlySoldOutProducts", orderService.getNearlySoldOutProduct());
        return "pages/seller/index";
    }

    @GetMapping("/seller-update-product/{id}")
    public String showUpdateProductForm(
            @PathVariable Long id,
            Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("units", unitService.getAllUnits());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("updateProductDto", productService.mappingProductDtoFromProduct(product));
        return "pages/seller/product/update-product";
    }

    @PostMapping("/seller-update-product")
    public String handleUpdateProduct(
            @Valid @ModelAttribute UpdateProductDto updateProductDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam MultipartFile imageFile,
            @RequestParam MultipartFile[] subImageFiles,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        try {
            Product oldProduct = productService.getProductById(updateProductDto.getId());
            List<Category> categories = new ArrayList<>();
            List<SubImage> subImages = new ArrayList<>();
            for (Long catId : updateProductDto.getCategories()) {
                categories.add(categoryService.getCategoryById(catId));
            }
            // if (!updateProductDto.getName().equals(oldProduct.getName())
            //         && productService.checkUniqueProductName(updateProductDto.getName())) {
            //     throw new Exception("Tên sản phẩm đã tồn tại");
            // }
            Product updateProduct = Product.builder()
                    .id(updateProductDto.getId())
                    .name(updateProductDto.getName())
                    .description(updateProductDto.getDescription())
                    .price(updateProductDto.getPrice())
                    .unit(updateProductDto.getUnit())
                    .enabled(updateProductDto.getEnabled())
                    .categories(categories)
                    .build();

            if (imageFile == null || imageFile.isEmpty()) {
                updateProduct.setMain_image_url(oldProduct.getMain_image_url());
            } else {
                updateProduct.setMain_image_url(ImageService.convertToBase64WithPrefix(imageFile));
            }
            for (int i = 0; i < subImageFiles.length; i++) {
                if (subImageFiles[i] == null || subImageFiles[i].isEmpty()) {
                    subImages.add(oldProduct.getSub_images().get(i));
                } else {
                    SubImage sub = new SubImage();
                    sub.setProduct(updateProduct);
                    sub.setSub_image_url(ImageService.convertToBase64WithPrefix(subImageFiles[i]));
                    subImages.add(sub);
                }
            }
            updateProduct.setSub_images(subImages);
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
        newProduct.setCategories(categoryService.getAllCategories());
        model.addAttribute("productDto", newProduct);
        model.addAttribute("units", unitService.getAllUnits());
        return "pages/seller/product/create-product";
    }

    @PostMapping("/seller-create-product")
    public String handleCreateProduct(
            @Valid @ModelAttribute CreateProductDto productDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            Model model) {
        try {
            Product product = productService.createProductForAddRequest(productDto, bindingResult);
            sellerRequestService.saveAddRequest(product, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu tạo sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/seller-create-product";
    }

    @GetMapping("/product-unit")
    public String getProductUnitList(Model model,
            @RequestParam(value = "allowDecimal", required = false) Boolean allowDecimal) {
        List<ProductUnit> productUnits;

        if (allowDecimal != null) {
            productUnits = productUnitService.getUnitsByAllowDecimal(allowDecimal);
        } else {
            productUnits = productUnitService.getAllProductUnit();
        }

        model.addAttribute("productUnits", productUnits);
        model.addAttribute("allowDecimal", allowDecimal);
        return "pages/seller/product/product-unit";
    }

    @GetMapping("/product-category")
    public String getAllProductUnit(Model model,
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<Category> categories = categoryService.searchByCategoryName(categoryName, size, page);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryName", categoryName);
        return "pages/seller/product/product-category";
    }

    @GetMapping("/create-product-unit")
    public String showCreateProductUnitForm(Model model) {
        CreateProductUnitDto createProductUnitDto = new CreateProductUnitDto();
        model.addAttribute("productUnitDto", createProductUnitDto);
        return "pages/seller/product/create-product-unit";
    }

    @PostMapping("/create-product-unit")
    public String handleCreateProductUnit(
            @Valid @ModelAttribute("productUnitDto") CreateProductUnitDto productUnitDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/create-product-unit";
        }
        try {
            ProductUnit productUnit = ProductUnit.builder()
                    .name(productUnitDto.getName())
                    .isAllowDecimal(productUnitDto.getIsAllowDecimal())
                    .build();
            
            sellerRequestService.saveAddRequest(productUnit, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu tạo đơn vị sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/create-product-unit";
    }

    @GetMapping("/edit-product-unit")
    public String showEditProductUnitForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductUnit productUnit = productUnitService.getProductUnitById(id);
            if (productUnit == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn vị sản phẩm");
                return "redirect:/seller/product-unit";
            }
            
            UpdateProductUnitDto updateProductUnitDto = UpdateProductUnitDto.builder()
                    .id(productUnit.getId())
                    .name(productUnit.getName())
                    .isAllowDecimal(productUnit.isAllowDecimal())
                    .build();
                    
            model.addAttribute("updateProductUnitDto", updateProductUnitDto);
            return "pages/seller/product/edit-product-unit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/seller/product-unit";
        }
    }

    @PostMapping("/edit-product-unit")
    public String handleEditProductUnit(
            @Valid @ModelAttribute("updateProductUnitDto") UpdateProductUnitDto updateProductUnitDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/edit-product-unit?id=" + updateProductUnitDto.getId();
        }
        try {
            ProductUnit oldProductUnit = productUnitService.getProductUnitById(updateProductUnitDto.getId());
            if (oldProductUnit == null) {
                throw new Exception("Không tìm thấy đơn vị sản phẩm");
            }
            
            ProductUnit newProductUnit = ProductUnit.builder()
                    .id(updateProductUnitDto.getId())
                    .name(updateProductUnitDto.getName())
                    .isAllowDecimal(updateProductUnitDto.getIsAllowDecimal())
                    .build();
            
            sellerRequestService.saveUpdateRequest(oldProductUnit, newProductUnit, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu cập nhật đơn vị sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/edit-product-unit?id=" + updateProductUnitDto.getId();
        }
        return "redirect:/seller/product-unit";
    }

    @GetMapping("/delete-product-unit")
    public String deleteProductUnit(
            @RequestParam Long id,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            ProductUnit productUnit = productUnitService.getProductUnitById(id);
            if (productUnit == null) {
                throw new Exception("Không tìm thấy đơn vị sản phẩm");
            }
            
            // Check if the product unit is being used by any products
            if (productUnit.getProducts() != null && !productUnit.getProducts().isEmpty()) {
                throw new Exception("Không thể xóa đơn vị sản phẩm này vì đang được sử dụng bởi " 
                    + productUnit.getProducts().size() + " sản phẩm");
            }
            
            sellerRequestService.saveDeleteRequest(productUnit, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu xóa đơn vị sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/product-unit";
    }
}
