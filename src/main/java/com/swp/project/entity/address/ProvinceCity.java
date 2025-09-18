package com.swp.project.entity.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class ProvinceCity implements Serializable {
    @Id
    private String code;

    private String name;

    @OneToMany(mappedBy = "provinceCity", fetch = FetchType.EAGER)
    private List<CommuneWard> communeWards;
}