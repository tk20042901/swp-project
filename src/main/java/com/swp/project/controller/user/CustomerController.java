package com.swp.project.controller.user;

import com.swp.project.dto.ChangePasswordDto;
import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.entity.user.Customer;
import com.swp.project.service.CustomerAiService;
import com.swp.project.service.AddressService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAiService customerAiService;
    private final AddressService addressService;
    private final ProductService productService;

    @GetMapping("/account-manager")
    public String accountManager() {
        return "pages/customer/account-manager/account-manager";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "pages/customer/account-manager/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute ChangePasswordDto changePasswordDto,
                                        BindingResult bindingResult,
                                        Model model,
                                        Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("changePasswordRequest", changePasswordDto);
            return "pages/customer/account-manager/change-password";
        }

        try {
            customerService.changePassword(principal.getName(), changePasswordDto);
            model.addAttribute("success", "Thay đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pages/customer/account-manager/change-password";
    }

    @GetMapping("/delivery-info")
    public String deliveryInfo(Model model, Principal principal) {
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
            redirectAttributes.addFlashAttribute("wards",
                    addressService.getAllCommuneWardByProvinceCityCode(
                            deliveryInfoDto.getProvinceCityCode()));
            return "redirect:/customer/delivery-info";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("deliveryInfoDto", deliveryInfoDto);
            model.addAttribute("provinceCities", addressService.getAllProvinceCity());
            model.addAttribute("wards",
                    addressService.getAllCommuneWardByProvinceCityCode(
                            deliveryInfoDto.getProvinceCityCode()));
            return "pages/customer/account-manager/delivery-info";
        }

        customerService.updateDeliveryInfo(principal.getName(), deliveryInfoDto);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin giao hàng thành công");
        return "redirect:/customer/delivery-info";
    }

    @GetMapping("/shopping-cart")
    public String viewShoppingCart(Model model, Principal principal) {
        List<ShoppingCartItem> cartItems = customerService.getCart(principal.getName());
        long totalAmmount = customerService.TotalAmountInCart(principal.getName());
        model.addAttribute("totalAmmount", totalAmmount);
        model.addAttribute("cartItems", cartItems);
        return "pages/customer/shopping-cart";
    }

    @PostMapping("/shopping-cart/remove")
    public String removeFromCart(@RequestParam Long productId, Principal principal) {
        customerService.removeItem(principal.getName(), productId);
        return "redirect:/customer/shopping-cart";
    }

    @PostMapping("/shopping-cart/update")
    public String updateCartItem(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 Principal principal) {

        customerService.updateCartQuantity(principal.getName(), productId, quantity);
        return "redirect:/customer/shopping-cart";
    }

    @PostMapping("/shopping-cart/check-out")
    public String checkOut(@RequestParam List<Long> cartIds,
                           RedirectAttributes redirectAttributes,
                           Principal principal) {
        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();
        cartIds.forEach(i -> shoppingCartItems.add(productService.getAllShoppingCartItemByCustomerIdAndProductId(principal.getName(), i)));
        redirectAttributes.addFlashAttribute("shoppingCartItems", shoppingCartItems);
        return "redirect:/order/order-info";
    }

    @GetMapping("/ai")
    public String ask(Model model) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        return "pages/customer/ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId,
                      @RequestParam String q,
                      @RequestParam MultipartFile image,
                      Model model) {
        try {
            customerAiService.ask(conversationId, q, image);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("conversation", customerAiService.getConversation(conversationId));
        return "pages/customer/ai";
    }


}

