package com.swp.project.entity.user;


import com.swp.project.entity.address.CommuneWard;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

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
    @JoinColumn(name = "commune_ward_code", referencedColumnName = "code", nullable = false)
    private CommuneWard communeWard;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("Seller"));
    }
}
