package com.swp.project.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer-support")
public class CustomerSupportController {
    @RequestMapping("")
    public String index() {
        return "pages/customer-support/index";
    }
}
