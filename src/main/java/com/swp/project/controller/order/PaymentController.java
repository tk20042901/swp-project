package com.swp.project.controller.order;


import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.user.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class PaymentController {

    private final PayOS payOS;
    private final OrderService orderService;
    private final CustomerService customerService;

    private static final String returnUrl = "http://swp-project.loca.lt/order/success";
    private static final String cancelUrl = "http://swp-project.loca.lt/order/cancel";

    @GetMapping("/checkout")
    public void checkout(@ModelAttribute("orderId") Long orderId,
                         HttpServletResponse httpServletResponse) {
        try {
            Order order = orderService.getOrderById(orderId);
            List<ItemData> items =
                    order.getOrderItem().stream().map(orderDetail -> ItemData.builder()
                            .name(orderDetail.getProduct().getName())
                            .price(orderDetail.getProduct().getPrice())
                            .quantity(orderDetail.getQuantity())
                            .build()).toList();
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(order.getId())
                    .buyerName(order.getCustomer().getName())
                    .buyerEmail(order.getCustomer().getEmail())
                    .buyerPhone(order.getCustomer().getPhoneNumber())
                    .buyerAddress(customerService.getAddressString(order.getCustomer().getEmail()))
                    .amount(orderService.totalAmount(orderId))
                    .description("Don hang" + order.getId())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .items(items)
                    .build();
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            httpServletResponse.setHeader("Location", data.getCheckoutUrl());
            httpServletResponse.setStatus(302);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
