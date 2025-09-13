package com.swp.project.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @GetMapping("")
    public String shipperMain() {
        return "pages/shipper/index";
    }
}
