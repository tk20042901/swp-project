package com.swp.project.controller.payment;


import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CheckoutController {

    private final PayOS payOS;
    private final OrderService orderService;

    private static final String returnUrl = "https://swp-project.loca.lt/customer/order-success";
    private static final String cancelUrl = "https://swp-project.loca.lt/customer/order-cancel";

    @GetMapping("/checkout")
    public void checkout(@RequestParam Long orderId,
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
                    .buyerAddress(order.getAddressString())
                    .amount(order.getTotalAmount())
                    .description("Don hang " + order.getId())
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
