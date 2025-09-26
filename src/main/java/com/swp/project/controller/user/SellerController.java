package com.swp.project.controller.user;

import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        // TODO: check and warning inventory if order is pending confirmation status
        model.addAttribute("orderStatusService", orderStatusService);
        model.addAttribute("order", orderService.getOrderById(orderId));
        return "pages/seller/order/order-detail";
    }

    @PostMapping("/update-pending-order-status")
    public String updatePendingOrderStatus(@RequestParam Long orderId,
                                           @RequestParam String action,
                                           RedirectAttributes redirectAttributes) {
        if (action.equals("accept")) {
            //TODO: check inventory before accepting order
            //TODO: reduce inventory
            redirectAttributes.addFlashAttribute("msg",
                    "Chấp nhận đơn hàng thành công");
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
        orderService.setOrderStatus(orderId, orderStatusService.getAwaitingShipmentStatus());
        redirectAttributes.addFlashAttribute("msg",
                "Cập nhật trạng thái đơn hàng thành Đang chờ giao hàng thành công");
        return "redirect:/seller/all-orders";
    }
}
