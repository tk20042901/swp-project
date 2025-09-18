package com.swp.project.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller("/order")
public class OrderController {

    @GetMapping("/{orderId}/delivery-info")
    public String showDeliveryInfoForm(@PathVariable Long orderId) {

        return "pages/order/delivery-info";
    }

    @GetMapping(value = "/{orderId}/success")
    public String success(@PathVariable Long orderId, Model model) {

        return "pages/order/success";
    }

    @GetMapping(value = "/{orderId}/cancel")
    public String cancel(@PathVariable Long orderId, Model model) {

        return "pages/order/cancel";
    }
}
