package com.swp.project.entity.shopping_cart;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.user.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ShoppingCartItem implements Serializable {
    @EmbeddedId
    private ShoppingCartItemId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("customerId")
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("productId")
    private Product product;

    @Column(nullable = false)
    private int quantity;

}
