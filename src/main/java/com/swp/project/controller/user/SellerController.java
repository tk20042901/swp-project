package com.swp.project.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    @GetMapping("")
    public String sellerMain() {
        return "pages/seller/index";
    }
}
