package com.swp.project.repository.address;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneWardRepository extends JpaRepository<CommuneWard,String> {

    List<CommuneWard> getByProvinceCity_Code(String provinceCityCode);

    CommuneWard getByCode(String code);

    List<CommuneWard> findAllByProvinceCity(ProvinceCity provinceCity);
}
