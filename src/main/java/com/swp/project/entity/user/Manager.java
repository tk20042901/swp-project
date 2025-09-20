package com.swp.project.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table
@PrimaryKeyJoinColumn(name = "id")
public class Manager extends User {
    @Column(length = 100, nullable = false)
    private String fullname;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate birthDate;

    @Column(length = 50, unique = true, nullable = false)
    private String cid;

    private String provinceCityCode;

    private String communeWardCode;

    private String address;

    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    private String specificAddress;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("Manager"));
    }
}
