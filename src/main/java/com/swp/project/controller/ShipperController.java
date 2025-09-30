package com.swp.project.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.user.ShipperService;

import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    private final ShipperService shipperService;
    private final OrderService orderService;

    @GetMapping("")
    public String shipperMain() {
        return "pages/shipper/index";
    }

    @GetMapping("/orders")
    public String shipperOrders(Model model,
                                Principal principal,
                                @RequestParam(defaultValue = "1") int pageDelivering,
                                @RequestParam(defaultValue = "1") int pagePending,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            // Lấy Page<Order> thay vì List<Order>
            Page<Order> deliveringOrders = shipperService.getDeliveringOrders(principal, pageDelivering, size);
            model.addAttribute("deliveringOrders", deliveringOrders.getContent());
            model.addAttribute("currentPageDelivering", pageDelivering);
            model.addAttribute("totalPagesDelivering", deliveringOrders.getTotalPages());

            Page<Order> pendingOrders = shipperService.getPendingOrders(principal, pagePending, size);
            model.addAttribute("pendingOrders", pendingOrders.getContent());
            model.addAttribute("currentPagePending", pagePending);
            model.addAttribute("totalPagesPending", pendingOrders.getTotalPages());

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("orderStatusService", shipperService.getOrderStatusService());
        return "pages/shipper/orders";
    }

    @PostMapping("/orders/done/{orderId}")
    public String donePost(@PathVariable Long orderId,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            shipperService.markOrderAsDelivered(orderId, principal);
            redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được đánh dấu là hoàn thành.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/orders/deliver/{orderId}")
    public String deliverPost(@PathVariable Long orderId,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            shipperService.deliverOrder(orderId, principal);
            redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được nhận để giao.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId,
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByOrderId(orderId);
            Long totalAmount = orderService.calculateTotalAmount(order);
            model.addAttribute("order", order);
            model.addAttribute("totalAmount", totalAmount);
            return "pages/shipper/order_details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/shipper/orders";
        }
    }
    
    
}
