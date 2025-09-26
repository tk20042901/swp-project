package com.swp.project.service.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.repository.address.CommuneWardRepository;

@Service
public class CommuneWardService {
    @Autowired
    private CommuneWardRepository communeWardRepository;

    public List<CommuneWard> findByProvinceCity(ProvinceCity provinceCity) {
        return communeWardRepository.findByProvinceCity(provinceCity);
    }
}
