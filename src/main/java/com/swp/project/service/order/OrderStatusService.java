package com.swp.project.service.order;

import com.swp.project.entity.order.OrderStatus;
import com.swp.project.repository.order.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    public OrderStatus getPendingConfirmationStatus() {
        return orderStatusRepository.findByName("Chờ Xác Nhận");
    }

    public OrderStatus getPendingPaymentStatus() {
        return orderStatusRepository.findByName("Chờ Thanh Toán");
    }

    public OrderStatus getAwaitingShipmentStatus() {
        return orderStatusRepository.findByName("Chờ Giao Hàng");
    }

    public OrderStatus getShippingStatus() {
        return orderStatusRepository.findByName("Đang Giao Hàng");
    }

    public OrderStatus getDeliveredStatus() {
        return orderStatusRepository.findByName("Đã Giao Hàng");
    }

    public OrderStatus getCompletedStatus() {
        return orderStatusRepository.findByName("Đã Hoàn Thành");
    }

    public OrderStatus getCancelledStatus() {
        return orderStatusRepository.findByName("Đã Hủy");
    }
}
