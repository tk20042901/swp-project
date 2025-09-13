package com.swp.project.entity.shopping_cart;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ShoppingCartItemId implements Serializable {
    private Long customerId;
    private Long productId;
}
