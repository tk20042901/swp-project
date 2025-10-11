package com.swp.project.repository.address;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;

@Repository
public interface CommuneWardRepository extends JpaRepository<CommuneWard,String> {

    List<CommuneWard> getByProvinceCity_Code(String provinceCityCode);

    CommuneWard getByCode(String code);

    List<CommuneWard> findByProvinceCity(ProvinceCity provinceCity);

    boolean existsByCode(String communeWard);
}
