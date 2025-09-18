package com.swp.project.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RequiredArgsConstructor
@RestController
public class PaymentConfirmationController {

    private final PayOS payOS;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;

    // Xử lý đơn hàng khi thanh toán thành công
    private void orderFulfilled(WebhookData data) {
        System.out.println("Payment successful");
        orderService.pickProductForOrder(data.getOrderCode());
        orderService.setOrderStatus(data.getOrderCode(),orderStatusService.getShippingStatus());
    }

    @PostMapping("/webhook")
    public ObjectNode payosWebhook(@RequestBody ObjectNode body){
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        try {
            orderFulfilled(payOS.verifyPaymentWebhookData(objectMapper.treeToValue(body, Webhook.class)));
            response.put("error", 0);
            response.put("message", "Webhook delivered");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
        }
        response.set("data", null);
        return response;
    }
}
