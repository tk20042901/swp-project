package com.swp.project.controller.order;


import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.user.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.List;

@RequiredArgsConstructor
@Controller("/order")
public class PaymentController {

    private final PayOS payOS;
    private final OrderService orderService;
    private final CustomerService customerService;


    private static final String returnUrl = "http://swp-project.loca.lt/success";
    private static final String cancelUrl = "http://swp-project.loca.lt/cancel";

    @PostMapping(value = "/{orderId}/checkout")
    public void checkout(HttpServletResponse httpServletResponse,
                         @PathVariable Long orderId) {
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
                    .description("Thanh toan cho don hang " + order.getId())
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
