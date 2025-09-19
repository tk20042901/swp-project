package com.swp.project.entity.user;


import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.swp.project.entity.address.CommuneWard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table
@PrimaryKeyJoinColumn(name = "id")
public class Seller extends User{

    @Column(length = 100, nullable = false)
    private String fullname;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Column(length = 50, unique = true, nullable = false)
    private String cId;

    @ManyToOne(fetch =  FetchType.EAGER)
    private CommuneWard communeWard;

    @Column(length = 100)
    private String specificAddress;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("Seller"));
    }
    
    public String getAddress() {
        if (communeWard == null) {
            return specificAddress;
        }
        return specificAddress + ", " + communeWard;
    }
}
