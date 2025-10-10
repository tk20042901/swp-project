package com.swp.project.controller;

import com.swp.project.dto.UpdateShoppingCartDto;
import com.swp.project.dto.ChangePasswordDto;
import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.service.AddressService;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.order.PaymentMethodService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.payos.PayOS;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@SessionAttributes("shoppingCartItems")
@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final AddressService addressService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
    private final PaymentMethodService paymentMethodService;
    private final PayOS payOS;

    @GetMapping("/account-manager")
    public String accountManager(Model model, Principal principal) {
        model.addAttribute("customer", customerService.getCustomerByEmail(principal.getName()));
        model.addAttribute("isGoogleRegistered",
                customerService.isGoogleRegistered(principal.getName()));
        return "pages/customer/account-manager/account-manager";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model, Principal principal) {
        if (customerService.isGoogleRegistered(principal.getName())) {
            return "redirect:/";
        }
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "pages/customer/account-manager/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute ChangePasswordDto changePasswordDto,
                                        BindingResult bindingResult,
                                        Model model,
                                        RedirectAttributes redirectAttributes,
                                        Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("changePasswordRequest", changePasswordDto);
            return "pages/customer/account-manager/change-password";
        }

        try {
            customerService.changePassword(principal.getName(), changePasswordDto);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
            return "redirect:/customer/account-manager";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/customer/account-manager/change-password";
        }
    }

    @GetMapping("/delivery-info")
    public String deliveryInfo(Model model, Principal principal) {
        if (!model.containsAttribute("deliveryInfoDto")) {
            DeliveryInfoDto deliveryInfoDto = new DeliveryInfoDto();
            if(deliveryInfoDto.setFromExistedInfo(customerService.getCustomerByEmail(principal.getName()))){
                model.addAttribute("wards", addressService
                        .getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            }
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
        }
        model.addAttribute("provinceCities", addressService.getAllProvinceCity());
        return "pages/customer/account-manager/delivery-info";
    }

    @PostMapping("/delivery-info")
    public String processDeliveryInfo(@Valid @ModelAttribute DeliveryInfoDto deliveryInfoDto,
                                      BindingResult bindingResult,
                                      @RequestParam(required = false) String update,
                                      Model model,
                                      RedirectAttributes redirectAttributes,
                                      Principal principal) {
        if (update == null) {
            redirectAttributes.addFlashAttribute("deliveryInfoDto", deliveryInfoDto);
            redirectAttributes.addFlashAttribute("wards", addressService
                    .getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            return "redirect:/customer/delivery-info";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
            model.addAttribute("provinceCities", addressService.getAllProvinceCity());
            model.addAttribute("wards", addressService
                    .getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            return "pages/customer/account-manager/delivery-info";
        }

        customerService.updateDeliveryInfo(principal.getName(), deliveryInfoDto);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin giao hàng thành công");
        return "redirect:/customer/account-manager";
    }

    @GetMapping("/shopping-cart")
    public String viewShoppingCart(Model model, HttpSession session,
                                   Principal principal) {
        List<ShoppingCartItem> cartItems = customerService.getCart(principal.getName());
        for(ShoppingCartItem item: cartItems) {
        int availableQuantity = productService.getAvailableQuantity(item.getProduct().getId());
        if (availableQuantity <= 0) {
            customerService.removeItem(principal.getName(), item.getProduct().getId());
        }
    }

        List<Long> selectedIds = (List<Long>) session.getAttribute("selectedIds");
        if (selectedIds == null) {
            selectedIds = new ArrayList<>();
        }


        int totalAmount = 0;
        for (ShoppingCartItem item : cartItems) {
            if (selectedIds.contains(item.getProduct().getId())) {
                totalAmount += item.getProduct().getPrice() * item.getQuantity();
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("totalAmount", totalAmount);

        return "pages/customer/shopping-cart";
    }
    @PostMapping("/shopping-cart/select")
    public String selectItem(@RequestParam Long cartIds,
                             HttpSession session) {
        List<Long> selectedIds = (List<Long>) session.getAttribute("selectedIds");
        if (selectedIds == null) {
            selectedIds = new ArrayList<>();
        }

        if (selectedIds.contains(cartIds)) {
            selectedIds.remove(cartIds);
        } else {
            selectedIds.add(cartIds);
        }

        session.setAttribute("selectedIds", selectedIds);
        return "redirect:/customer/shopping-cart";
    }
    @PostMapping("/shopping-cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        List<Long> selectedIds = (List<Long>) session.getAttribute("selectedIds");
        if (selectedIds != null && selectedIds.contains(productId)) {
            selectedIds.remove(productId);
            session.setAttribute("selectedIds", selectedIds);
        }
        customerService.removeItem(principal.getName(), productId);
        redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm khỏi giỏ hàng thành công");
        return "redirect:/customer/shopping-cart";
    }


    @PostMapping("/shopping-cart/update")
    public String updateCartItem(@Valid UpdateShoppingCartDto updateShoppingCartDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/customer/shopping-cart";
        }

        String quantityStr = updateShoppingCartDto.getQuantity();
        int quantity = Integer.parseInt(quantityStr);
        Long productId = updateShoppingCartDto.getProductId();
        int availableQuantity = productService.getAvailableQuantity(productId);
        if (quantity > availableQuantity) {
            quantity = availableQuantity;
            redirectAttributes.addFlashAttribute("error",
                    "Số lượng bạn chọn đã vượt quá tồn kho. Hệ thống đã điều chỉnh về " + availableQuantity);
        }
        customerService.updateCartQuantity(principal.getName(), productId, quantity);
        return "redirect:/customer/shopping-cart";
    }

    @PostMapping("/shopping-cart/check-out")
    public String checkOut(@RequestParam List<Long> cartIds,
                           Model model,
                           Principal principal) {
        List<ShoppingCartItem> shoppingCartItems = cartIds.stream()
                .map(cartId -> productService
                        .getShoppingCartItemByCustomerEmailAndProductId(principal.getName(), cartId))
                .filter(Objects::nonNull)
                .toList();
        model.addAttribute("shoppingCartItems", shoppingCartItems);
        return "redirect:/customer/order-info";
    }

    @GetMapping("/order-history")
    public String getOrderHistory(Model model,
                                  Principal principal,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                  @RequestParam(required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                  @RequestParam(defaultValue = "0")int page,
                                  @RequestParam(defaultValue = "5")int size){
        Page<Order> orders=customerService.searchOrderHistory(principal.getName(), status, fromDate, toDate, page, size);

        int totalSpent=0;
        for(Order order : orders.getContent()){
            if(orderStatusService.isDeliveredStatus(order)){
                totalSpent+=order.getTotalAmount();
            }
        }
        model.addAttribute("status",status);
        model.addAttribute("fromDate",fromDate);
        model.addAttribute("toDate",toDate);
        model.addAttribute("totalSpent",totalSpent);
        model.addAttribute("orders",orders );
        return "pages/customer/order/order-history";
    }
    @PostMapping("/order-history/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId ,RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(orderId);
        if(orderStatusService.isPendingConfirmationStatus(order)){
            orderService.setOrderStatus(orderId,orderStatusService.getCancelledStatus());
            redirectAttributes.addFlashAttribute("success","Hủy đơn hàng thành công");
        }

        return "redirect:/customer/order-history";
    }


    @GetMapping("/order/order-detail/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model){
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order",order);
        return "pages/customer/order/order-detail";
    }

    @GetMapping("/order-info")
    public String showOrderInfoForm(@ModelAttribute("shoppingCartItems") List<ShoppingCartItem> shoppingCartItems,
                                    Model model,
                                    Principal principal) {
        if (!model.containsAttribute("deliveryInfoDto")) {
            DeliveryInfoDto deliveryInfoDto = new DeliveryInfoDto();
            if(deliveryInfoDto.setFromExistedInfo(customerService.getCustomerByEmail(principal.getName()))){
                model.addAttribute("wards", addressService
                        .getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            }
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
        }
        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());
        model.addAttribute("provinceCities", addressService.getAllProvinceCity());
        model.addAttribute("shoppingCartItems", shoppingCartItems);
        model.addAttribute("totalAmount", shoppingCartItems.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity()).sum());
        return "pages/customer/order/order-info";
    }

    @PostMapping("/order-info")
    public String processOrder(@Valid @ModelAttribute DeliveryInfoDto deliveryInfoDto,
                               BindingResult bindingResult,
                               @ModelAttribute("shoppingCartItems") List<ShoppingCartItem> shoppingCartItems,
                               @RequestParam(required = false) String paymentMethodId,
                               @RequestParam(required = false) String confirm,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               SessionStatus sessionStatus,
                               Principal principal) {
        if (confirm == null) {
            redirectAttributes.addFlashAttribute("deliveryInfoDto", deliveryInfoDto);
            redirectAttributes.addFlashAttribute("wards",
                    addressService.getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            return "redirect:/customer/order-info";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());
            model.addAttribute("provinceCities", addressService.getAllProvinceCity());
            model.addAttribute("wards", addressService
                    .getAllCommuneWardByProvinceCityCode(deliveryInfoDto.getProvinceCityCode()));
            model.addAttribute("totalAmount", shoppingCartItems.stream().mapToInt
                    (item -> item.getProduct().getPrice() * item.getQuantity()).sum());
            return "/pages/customer/order/order-info";
        }

        if(paymentMethodId == null || paymentMethodId.isEmpty()){
            redirectAttributes.addFlashAttribute("paymentMethodError", "Vui lòng chọn phương thức thanh toán");
            return "redirect:/customer/order-info";
        }

        sessionStatus.setComplete();

        try {
            if (paymentMethodId.equals("COD")) {
                orderService.createCodOrder(principal.getName(), shoppingCartItems,deliveryInfoDto);
                return "redirect:/customer/order-success";
            } else {
                Order order = orderService.createQrOrder(principal.getName(), shoppingCartItems,deliveryInfoDto);
                return "redirect:/customer/pay-os-checkout?orderId=" + order.getId();
            }
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/shopping-cart";
        }
    }

    @GetMapping("/pay-os-checkout")
    public String payOsCheckout(@RequestParam Long orderId) {
        Order order = orderService.getOrderById(orderId);
        try {
            List<ItemData> items = order.getOrderItem().stream().map(orderItem ->
                    ItemData.builder()
                            .name(orderItem.getProduct().getName())
                            .price(orderItem.getProduct().getPrice())
                            .quantity(orderItem.getQuantity())
                            .build()).toList();
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(order.getId())
                    .buyerName(order.getCustomer().getName())
                    .buyerEmail(order.getCustomer().getEmail())
                    .buyerPhone(order.getCustomer().getPhoneNumber())
                    .buyerAddress(order.getAddressString())
                    .amount(order.getTotalAmount())
                    .expiredAt(order.getPaymentExpiredAt().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .items(items)
                    .description("FruitShop " + order.getId())
                    .returnUrl("http://localhost:8080/customer/order-success")
                    .cancelUrl("http://localhost:8080/customer/order-cancel")
                    .build();
            String checkoutUrl = payOS.createPaymentLink(paymentData).getCheckoutUrl();
            order.setPaymentLink(checkoutUrl);
            orderService.saveOrder(order);
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/customer/shopping-cart";
        }
    }

    @GetMapping(value = "/order-success")
    public String orderSuccess() {
        return "pages/customer/order/success";
    }

    @GetMapping(value = "/order-cancel")
    public String cancelPayment(@RequestParam Long orderCode,
                                @RequestParam boolean cancel) {
        if (cancel) {
            orderService.setOrderStatus(orderCode, orderStatusService.getCancelledStatus());
            return "pages/customer/order/cancel";
        }
        return "redirect:/";
    }

}