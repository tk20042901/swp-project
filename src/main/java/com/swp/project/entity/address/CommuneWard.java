package com.swp.project.entity.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommuneWard implements Serializable {
    @Id
    private String code;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProvinceCity provinceCity;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "communeWard")
    private List<Seller> sellers;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "communeWard")
    private List<Shipper> shippers;

    @Override
    public String toString() {
        return name + ", " + provinceCity.getName();
    }
}
