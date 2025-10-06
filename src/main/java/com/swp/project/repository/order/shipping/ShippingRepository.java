package com.swp.project.repository.order.shipping;

import com.swp.project.entity.order.shipping.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping,Integer> {
}
