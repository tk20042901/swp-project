package com.swp.project.entity.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

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

    @Override
    public String toString() {
        return name + ", " + provinceCity.getName();
    }
}
