package com.swp.project.repository.order.shipping;

import com.swp.project.entity.order.shipping.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingStatusRepository extends JpaRepository<ShippingStatus,Integer> {
    ShippingStatus getByDescription(String description);
}
