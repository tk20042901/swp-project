package com.swp.project.service.order;

import java.util.List;

import org.springframework.stereotype.Service;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.repository.order.OrderStatusRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    public List<OrderStatus> getAllStatus() {
        return orderStatusRepository.findAll();
    }

    public OrderStatus getOrderStatusById(Long orderId) {
        return orderStatusRepository.findById(orderId).orElse(null);
    }

    public OrderStatus getPendingConfirmationStatus() {
        return orderStatusRepository.findByName("Chờ Xác Nhận");
    }

    public OrderStatus getPendingPaymentStatus() {
        return orderStatusRepository.findByName("Chờ Thanh Toán");
    }

    public OrderStatus getProcessingStatus() {
        return orderStatusRepository.findByName("Đang Chuẩn Bị Hàng");
    }

    public OrderStatus getShippingStatus() {
        return orderStatusRepository.findByName("Đang Giao Hàng");
    }

    public OrderStatus getDeliveredStatus() {
        return orderStatusRepository.findByName("Đã Giao Hàng");
    }

    public OrderStatus getCancelledStatus() {
        return orderStatusRepository.findByName("Đã Hủy");
    }

    public boolean isPendingConfirmationStatus(Order order) {
        return order.getOrderStatus().getName().equals("Chờ Xác Nhận");
    }

    public boolean isPendingPaymentStatus(Order order) {
        return order.getOrderStatus().getName().equals("Chờ Thanh Toán");
    }

    public boolean isProcessingStatus(Order order) {
        return order.getOrderStatus().getName().equals("Đang Chuẩn Bị Hàng");
    }

    public boolean isShippingStatus(Order order) {
        return order.getOrderStatus().getName().equals("Đang Giao Hàng");
    }

    public boolean isDeliveredStatus(Order order) {
        return order.getOrderStatus().getName().equals("Đã Giao Hàng");
    }

    public boolean isCancelledStatus(Order order) {
        return order.getOrderStatus().getName().equals("Đã Hủy");
    }
}
