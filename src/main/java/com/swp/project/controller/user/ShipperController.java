package com.swp.project.controller.user;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.project.entity.order.Order;
import com.swp.project.service.user.ShipperService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/shipper")
public class ShipperController {
    private final ShipperService shipperService;

    @GetMapping("")
    public String shipperMain() {
        return "pages/shipper/index";
    }

    @GetMapping("/orders")
    public String shipperOrders(Model model,
                                Principal principal) {
        try {
            List<Order> deliveringOrders = shipperService.getPendingOrders(principal);
            model.addAttribute("deliveringOrders", deliveringOrders);

            List<Order> otherOrders = shipperService.getOtherOrders(principal);
            model.addAttribute("otherOrders", otherOrders);        
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("orderStatusService", shipperService.getOrderStatusService());
        return "pages/shipper/orders";
    }
}
