package com.swp.project.entity.user;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table
@PrimaryKeyJoinColumn(name = "id")
public class Customer extends User {
    @Column(length = 50)
    private String fullName;

    @Column(length = 15)
    private String phoneNumber;

    @ManyToOne(fetch =  FetchType.EAGER)
    private CommuneWard communeWard;

    @Column(length = 100)
    private String specificAddress;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ShoppingCartItem> shoppingCartItems;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("Customer"));
    }
}