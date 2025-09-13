package com.swp.project.entity.order;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
}
