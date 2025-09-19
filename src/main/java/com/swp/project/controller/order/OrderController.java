package com.swp.project.controller.order;

import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.entity.user.Customer;
import com.swp.project.service.AddressService;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.user.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@SessionAttributes("shoppingCartItems")
@RequiredArgsConstructor
@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
    private final CustomerService customerService;
    private final AddressService addressService;

    @GetMapping("/order-info")
    public String showOrderInfoForm(@ModelAttribute("shoppingCartItems") List<ShoppingCartItem> shoppingCartItems,
                                    Model model,
                                    Principal principal) {
        Customer customer = customerService.getCustomerByEmail(principal.getName());
        if (!model.containsAttribute("deliveryInfoDto")) {
            DeliveryInfoDto deliveryInfoDto = new DeliveryInfoDto();
            deliveryInfoDto.setFullName(customer.getFullName());
            deliveryInfoDto.setPhone(customer.getPhoneNumber());
            deliveryInfoDto.setSpecificAddress(customer.getSpecificAddress());
            if (customer.getCommuneWard() != null) {
                deliveryInfoDto.setProvinceCityCode(customer.getCommuneWard().getProvinceCity().getCode());
                deliveryInfoDto.setCommuneWardCode(customer.getCommuneWard().getCode());
                model.addAttribute("wards",
                        addressService.getAllCommuneWardByProvinceCityCode(
                                customer.getCommuneWard().getProvinceCity().getCode()));
            }
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
        }
        model.addAttribute("provinceCities", addressService.getAllProvinceCity());
        model.addAttribute("shoppingCartItems", shoppingCartItems);
        model.addAttribute("totalAmount",
                shoppingCartItems.stream().mapToInt(item ->
                        item.getProduct().getPrice() * item.getQuantity()).sum());
        return "pages/customer/order/order-info";
    }

    @PostMapping("/order-info")
    public String processOrder(@Valid @ModelAttribute DeliveryInfoDto deliveryInfoDto,
                               BindingResult bindingResult,
                               @ModelAttribute("shoppingCartItems") List<ShoppingCartItem> shoppingCartItems,
                               @RequestParam(name = "payment_method") String paymentMethod,
                               @RequestParam(required = false) String confirm,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        if (confirm == null) {
            redirectAttributes.addFlashAttribute("deliveryInfoDto", deliveryInfoDto);
            redirectAttributes.addFlashAttribute("wards",
                    addressService.getAllCommuneWardByProvinceCityCode(
                            deliveryInfoDto.getProvinceCityCode()));
            return "redirect:/order/order-info";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
            model.addAttribute("provinceCities", addressService.getAllProvinceCity());
            model.addAttribute("wards",
                    addressService.getAllCommuneWardByProvinceCityCode(
                            deliveryInfoDto.getProvinceCityCode()));
            model.addAttribute("totalAmount",
                    shoppingCartItems.stream().mapToInt(item ->
                            item.getProduct().getPrice() * item.getQuantity()).sum());
            return "/pages/customer/order/order-info";
        }

        Order order = orderService.createOrder(principal.getName(),
                shoppingCartItems,
                deliveryInfoDto.getFullName(),
                deliveryInfoDto.getPhone(),
                addressService.getCommuneWardByCode(deliveryInfoDto.getCommuneWardCode()),
                deliveryInfoDto.getSpecificAddress());

        if (paymentMethod.equals("cod")) {
            orderService.setOrderStatus(order.getId(), orderStatusService.getPendingConfirmationStatus());
            return "redirect:/order/success";
        } else {
            orderService.setOrderStatus(order.getId(), orderStatusService.getPendingPaymentStatus());
            return "redirect:/checkout?orderId=" + order.getId();
        }
    }

    @GetMapping(value = "/success")
    public String orderSuccess() {
        return "pages/customer/order/success";
    }

    @GetMapping(value = "/cancel")
    public String cancelPayment(@RequestParam Long orderCode,
                                @RequestParam boolean cancel){
        if(cancel) {
            orderService.setOrderStatus(orderCode, orderStatusService.getCancelledStatus());
            return "pages/customer/order/cancel";
        }
        return "redirect:/";
    }
}
