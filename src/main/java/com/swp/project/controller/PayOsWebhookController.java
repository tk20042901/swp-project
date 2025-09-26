package com.swp.project.controller;

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

    private void orderConfirmed(Long orderId) {
        //TODO: process payOS order confirmation logic
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
