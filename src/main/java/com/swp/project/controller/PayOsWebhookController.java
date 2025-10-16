package com.swp.project.controller;

import com.swp.project.entity.order.Order;
import com.swp.project.service.EmailService;
import com.swp.project.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
public class PayOsWebhookController {

    private final PayOS payOS;
    private final OrderService orderService;
    private final EmailService emailService;

    private void orderConfirmed(WebhookData paymentData) {
        Long orderId = paymentData.getOrderCode();
        Order order = orderService.getOrderById(orderId);
        orderService.doWhenOrderConfirmed(order);
        orderService.createBillForOrder(order);
        emailService.sendSimpleEmail(order.getCustomer().getEmail(),
                "Xác nhận thanh toán cho đơn hàng " + orderId + " thành công",
                "Đơn hàng " + orderId + " đã được thanh toán thành công với số tiền "
                        + paymentData.getAmount() + paymentData.getCurrency() + ".\n" +
                        "Thời gian thanh toán thành công: " + paymentData.getTransactionDateTime() + "\n" +
                        "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi!");
    }

    @PostMapping(path = "/webhook")
    public void payosTransferHandler(@RequestBody Object body) {
        WebhookData data = payOS.webhooks().verify(body);
        CompletableFuture.runAsync(() -> orderConfirmed(data));
    }
}