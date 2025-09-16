package com.swp.project.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

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

    @Column(length = 50)
    private String ward;

    @Column(length = 100)
    private String address;

    @Builder.Default
    private Long loyaltyPoints = 0L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("Customer"));
    }
}