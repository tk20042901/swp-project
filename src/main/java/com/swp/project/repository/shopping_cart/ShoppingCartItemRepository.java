package com.swp.project.repository.shopping_cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.entity.shopping_cart.ShoppingCartItemId;
import com.swp.project.entity.user.Customer;

@Repository
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, ShoppingCartItemId> {
    List<ShoppingCartItem> findByCustomer(Customer customer);

    ShoppingCartItem findShoppingCartItemById(ShoppingCartItemId id);

    ShoppingCartItem findByCustomer_EmailAndProduct_Id(String email, Long productId);

    @Modifying
    @Query("delete from ShoppingCartItem s where s.customer.email = ?1 and s.product.id = ?2")
    void deleteByCustomerEmailAndProductId(String email, Long productId);
}
