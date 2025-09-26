package com.swp.project.repository.address;

import com.swp.project.entity.address.ProvinceCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceCityRepository extends JpaRepository<ProvinceCity,String> {
    ProvinceCity getByCode(String provinceCity);
}
