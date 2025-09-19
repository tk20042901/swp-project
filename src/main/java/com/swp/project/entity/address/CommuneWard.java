package com.swp.project.entity.address;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
