package com.swp.project.controller.user;

import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final OrderStatusService orderStatusService;
    private final OrderService orderService;

    @GetMapping("")
    public String sellerMain() {
        return "pages/seller/index";
    }

    @GetMapping("/all-orders")
    public String sellerProducts(@PageableDefault(5) Pageable pageable,
                                 @ModelAttribute SellerSearchOrderDto sellerSearchOrderDto,
                                 Model model,
                                 HttpServletRequest request) {
        if(sellerSearchOrderDto.isEmpty()) {
            model.addAttribute("orders", orderService.getAllOrder(pageable));
        } else {
            model.addAttribute("orders",orderService.searchOrder(sellerSearchOrderDto,pageable));
        }
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("orderStatus", orderStatusService.getAllStatus());
        model.addAttribute("sellerSearchOrderDto", sellerSearchOrderDto);
        return "pages/seller/all-orders";
    }

    @GetMapping("/order-detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderStatusService", orderStatusService);
        model.addAttribute("order", orderService.getOrderById(orderId));
        return "pages/seller/order-detail";
    }

    @PostMapping("/update-pending-order-status")
    public String updatePendingOrderStatus(@RequestParam Long orderId,
                              @RequestParam String action,
                              RedirectAttributes redirectAttributes) {
        if(action.equals("accept")) {
            orderService.doWhenOrderConfirmed(orderId);
            redirectAttributes.addFlashAttribute("msg",
                    "Chấp nhận đơn hàng thành công");
        } else if(action.equals("reject")) {
            orderService.setOrderStatus(orderId,orderStatusService.getCancelledStatus());
            redirectAttributes.addFlashAttribute("msg",
                    "Từ chối đơn hàng thành công");
        }
        return "redirect:/seller/all-orders";
    }

    @PostMapping("/update-processing-order-status")
    public String updateProcessingOrderStatus(@RequestParam Long orderId,
                                              RedirectAttributes redirectAttributes) {
        orderService.setOrderStatus(orderId,orderStatusService.getAwaitingShipmentStatus());
        redirectAttributes.addFlashAttribute("msg",
                "Cập nhật trạng thái đơn hàng thành Đang chờ giao hàng thành công");
        return "redirect:/seller/all-orders";
    }
}
