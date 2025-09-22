package com.swp.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.service.product.ProductService;




@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product-details")
    public String getProductDetail(
            @RequestParam(name = "id") Long id,
            Model model) {
        Product product = productService.getProductById(id);
        List<SubImage> subImages = product.getSub_images();
        model.addAttribute("product", product);
        model.addAttribute("subImages", subImages);
        return "pages/product/product-details";
    }

}
