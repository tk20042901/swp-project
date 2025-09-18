package com.swp.project.repository.shopping_cart;

import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.entity.shopping_cart.ShoppingCartItemId;
import com.swp.project.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, ShoppingCartItemId> {
    List<ShoppingCartItem> findByCustomer(Customer customer);

    ShoppingCartItem findShoppingCartItemById(ShoppingCartItemId id);

    ShoppingCartItem findByCustomer_EmailAndProduct_Id(String email, Long productId);
}
