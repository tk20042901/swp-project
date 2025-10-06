package com.swp.project.service.order.shipping;

import com.swp.project.entity.order.shipping.ShippingStatus;
import com.swp.project.repository.order.shipping.ShippingStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ShippingStatusService {

    private final ShippingStatusRepository shippingStatusRepository;

    public ShippingStatus getAwaitingPickupStatus() {
        return shippingStatusRepository.getByDescription("Đang Lấy Hàng");
    }

    public ShippingStatus getPickedUpStatus() {
        return shippingStatusRepository.getByDescription("Đã Lấy Hàng");
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatusRepository.getByDescription("Đang Giao Hàng");
    }

    public ShippingStatus getDeliveredStatus() {
        return shippingStatusRepository.getByDescription("Đã Giao Hàng");
    }

    public boolean isAwaitingPickupStatus(ShippingStatus status) {
        return status.getDescription().equals("Đang Lấy Hàng");
    }

    public boolean isPickedUpStatus(ShippingStatus status) {
        return status.getDescription().equals("Đã Lấy Hàng");
    }

    public boolean isShippingStatus(ShippingStatus status) {
        return status.getDescription().equals("Đang Giao Hàng");
    }

    public boolean isDeliveredStatus(ShippingStatus status) {
        return status.getDescription().equals("Đã Giao Hàng");
    }
}
