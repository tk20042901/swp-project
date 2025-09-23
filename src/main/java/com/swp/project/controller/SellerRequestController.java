package com.swp.project.controller;

import com.swp.project.entity.product.ProductUnit;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import com.swp.project.service.seller_request.SellerRequestTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerRequestController {

    private final ProductUnitService productUnitService;
    private final SellerRequestService sellerRequestService;
    private final SellerRequestTypeService sellerRequestTypeService;

    @GetMapping("/add-product-unit-request")
    public String addProductUnitRequest() {
        return "add-product-unit-request";
    }

    @PostMapping("/add-product-unit-request")
    public String handleAddProductUnitRequest(@RequestParam String name,
                                              Principal principal) {
        ProductUnit productUnit = ProductUnit.builder()
                .name(name)
                .build();
        try {
            sellerRequestService.saveAddRequest(productUnit, principal.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/update-product-unit-request/{id}")
    public String updateProductUnitRequest(@PathVariable Long id,Model model) {
        model.addAttribute("productUnit", productUnitService.getProductUnitById(id));
        return "update-product-unit-request";
    }

    @PostMapping("/update-product-unit-request")
    public String handleUpdateProductUnitRequest(@RequestParam Long id,
                                                 @RequestParam String name,
                                                 Principal principal) {
        ProductUnit productUnit = ProductUnit.builder()
                .name(name)
                .build();
        ProductUnit oldProductUnit = productUnitService.getProductUnitById(id);
        try {
            sellerRequestService.saveUpdateRequest(oldProductUnit, productUnit, principal.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/all-seller-request")
    public String showAllSellerRequest(Model model) {
        model.addAttribute("requests", sellerRequestService.getAllSellerRequest());
        model.addAttribute("sellerRequestTypeService", sellerRequestTypeService);
        return "all-seller-request";
    }

    @GetMapping("/seller-add-product-unit-request-detail/{id}")
    public String showSellerAddRequestDetail(@PathVariable Long id, Model model) {
        model.addAttribute("sellerRequest",sellerRequestService.getSellerRequestById(id));
        return "seller-add-product-unit-request-detail";
    }

    @GetMapping("/seller-update-product-unit-request-detail/{id}")
    public String showSellerUpdateRequestDetail(@PathVariable Long id, Model model) {
        model.addAttribute("sellerRequest",sellerRequestService.getSellerRequestById(id));
        return "seller-add-product-unit-request-detail";
    }

    @PostMapping("/approve-seller-request")
    public String approveSellerRequest(@RequestParam Long id) {
        try {
            sellerRequestService.approveRequest(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @PostMapping("/reject-seller-request")
    public String rejectSellerRequest(@RequestParam Long id) {
        try {
            sellerRequestService.rejectRequest(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
}
