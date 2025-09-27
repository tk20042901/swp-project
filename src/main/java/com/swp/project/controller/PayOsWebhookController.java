package com.swp.project.controller;

import com.swp.project.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.type.Webhook;

@RequiredArgsConstructor
@RestController
public class PayOsWebhookController {

    private final PayOS payOS;
    private final OrderService orderService;

    private void orderConfirmed(Long orderId) {
        orderService.doWhenOrderConfirmed(orderId);
        //TODO: send invoice email to customer
    }

    @PostMapping("/webhook")
    public void payosWebhook(@RequestBody Webhook body) {
        try {
            orderConfirmed(payOS.verifyPaymentWebhookData(body).getOrderCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
