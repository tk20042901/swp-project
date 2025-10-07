package com.swp.project.controller;

import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.dto.UpdateProductDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final OrderStatusService orderStatusService;
    private final OrderService orderService;
    private final ShipperService shipperService;
    private final SellerService sellerService;
    private final ProductService productService;
    private final ProductUnitService unitService;
    private final CategoryService categoryService;
    private final SellerRequestService sellerRequestService;

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
        if(orderService.isOrderItemQuantityMoreThanAvailable(orderId)) {
            model.addAttribute("warning", "Cảnh báo: Một số sản phẩm trong đơn hàng này có số lượng lớn hơn số lượng hiện có trong kho.");
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
        orderService.updateOrderStatusToShipping(orderService.getOrderById(orderId));
        redirectAttributes.addFlashAttribute("msg",
                "Cập nhật trạng thái đơn hàng thành Đang giao hàng thành công.\n" +
                        "Hệ thống đã tự động phân công Shipper cho đơn hàng.");
        return "redirect:/seller/all-orders";
    }

    @GetMapping("/all-products")
    public String getAllProductList(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) Boolean enabled ,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5")int size,
                                    Model model){
        Pageable pageable= PageRequest.of(page,size);
        Page<Product> products= productService.searchProductForSeller(name, enabled, pageable);
        model.addAttribute("products", products);
        model.addAttribute("name", name);
        model.addAttribute("enabled", enabled);
    return "pages/seller/product/all-products";
    }
    @GetMapping("/product/product-detail/{productId}")
    public String getProductDetail(@PathVariable("productId") Long id, Model model){
        Product product= productService.getProductById(id);
        int availableQuantity = productService.getAvailableQuantity(id);
        model.addAttribute("availableQuantity",availableQuantity);
        model.addAttribute("product",product);
        return "pages/seller/product/product-detail";
    }

    @GetMapping("/statistic-report/overview")
    public String getOverviewReport(Model model){
        model.addAttribute("unitSold",orderService.getUnitSold());
        model.addAttribute("nearlySoldOutProducts", orderService.getNearlySoldOutProduct());
        model.addAttribute("nearlyExpiredProducts", orderService.getNearlyExpiredProduct());
        return "pages/seller/statistic-report/overview";
    }
    @GetMapping("/seller-update-product")
    public String showUpdateProductForm(
        @RequestParam(required = false) Long productId,
        Model model) {
        Product product;
        UpdateProductDto dto = new UpdateProductDto();
        if(productId == null){
            product = productService.getFirstEnabledProduct();
        }else{
            product = productService.getProductById(productId);
        }
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setUnit(product.getUnit());
        dto.setCategories(product.getCategories().stream().map(Category::getId).toList());
        dto.setDescription(product.getDescription());
        dto.setEnabled(product.isEnabled());

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
        Model model) {
        
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors:");
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        try {
            Product oldProduct = productService.getProductById(updateProductDto.getId());
            List<Category> categories = new ArrayList<>();
            for(Long catId : updateProductDto.getCategories()) {
                categories.add(categoryService.getCategoryById(catId));
            }
            Product updateProduct = new Product();
            updateProduct.setId(updateProductDto.getId());
            updateProduct.setName(updateProductDto.getName());
            updateProduct.setDescription(updateProductDto.getDescription());
            updateProduct.setPrice(updateProductDto.getPrice());
            updateProduct.setUnit(unitService.getProductUnitById(updateProductDto.getUnit().getId()));
            updateProduct.setEnabled(updateProductDto.getEnabled());
            updateProduct.setCategories(categories);
            updateProduct.setMain_image_url(oldProduct.getMain_image_url());
            updateProduct.setSub_images(oldProduct.getSub_images());
            updateProduct.setProductBatches(oldProduct.getProductBatches());
            updateProduct.setTotalQuantity(oldProduct.getTotalQuantity());


            if(updateProduct.equals(oldProduct)) {
                throw new Exception("Không có thay đổi nào để cập nhật");
            }

            sellerRequestService.saveUpdateRequest(
                oldProduct,
                updateProduct,
                principal.getName());

            redirectAttributes.addFlashAttribute("msg", "Yêu cầu cập nhật sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (updateProductDto.getId() != null) {
                return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
            } else {
                return "redirect:/seller/seller-update-product";
            }
        }
        return "redirect:/seller";
    }

    @PostMapping("seller-product-request")
    public String sellerRequest(
            @RequestParam String requestType,
            @RequestParam(required = false) Long productId,
            @ModelAttribute Product product,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (requestType.equals("add")) {
                sellerRequestService.saveAddRequest(product, principal.getName());
                redirectAttributes.addFlashAttribute("msg", "Yêu cầu thêm sản phẩm đã được gửi đến quản lý");
            } else if (requestType.equals("update")) {
                if (productId == null) {
                    redirectAttributes.addFlashAttribute("error", "ID sản phẩm không hợp lệ");
                    return "redirect:/seller/all-products";
                }
                Product existingProduct = productService.getProductById(productId);
                // All sellers can update all products since they work collaboratively
                sellerRequestService.saveUpdateRequest(existingProduct, product, principal.getName());
                redirectAttributes.addFlashAttribute("msg", "Yêu cầu cập nhật sản phẩm đã được gửi đến quản lý");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage());
        }

        return "redirect:/seller/all-products";
    }

}
