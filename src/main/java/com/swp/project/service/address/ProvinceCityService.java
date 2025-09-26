package com.swp.project.service.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.repository.address.ProvinceCityRepository;

@Service
public class ProvinceCityService {

    @Autowired
    private ProvinceCityRepository provinceCityRepository;

    public List<ProvinceCity> findAll() {
        return provinceCityRepository.findAll();
    }

    public Object getByCode(String provinceCity) {
        return provinceCityRepository.getByCode(provinceCity);
    }
}
