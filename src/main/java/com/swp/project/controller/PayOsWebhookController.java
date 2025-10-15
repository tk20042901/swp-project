package com.swp.project.controller;

import com.swp.project.entity.order.Order;
import com.swp.project.service.EmailService;
import com.swp.project.service.order.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

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
                        + paymentData.getAmount() + paymentData.getCurrency() +".\n" +
                        "Thời gian thanh toán thành công: " + paymentData.getTransactionDateTime() + "\n" +
                        "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi!");
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<WebhookData>> payosTransferHandler(@RequestBody Object body) throws IllegalArgumentException {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            orderConfirmed(data);
            System.out.println("ok");
            return ResponseEntity.ok(ApiResponse.success("Webhook delivered", data));
        } catch (Exception e) {
            System.out.println("lol");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse<T> {
    private Integer error;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
