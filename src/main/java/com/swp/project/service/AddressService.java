package com.swp.project.service;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.address.ProvinceCityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AddressService {

    private final ProvinceCityRepository provinceCityRepository;
    private final CommuneWardRepository communeWardRepository;

    public List<ProvinceCity> getAllProvinceCity() {
        return provinceCityRepository.findAll();
    }

    public ProvinceCity getProvinceCityByCode(String code) {
        return provinceCityRepository.getByCode(code);
    }

    public List<CommuneWard> getAllCommuneWardByProvinceCityCode(String provinceCityCode) {
        return communeWardRepository.getByProvinceCity_Code(provinceCityCode);
    }

    public CommuneWard getCommuneWardByCode(String code) {
        return communeWardRepository.getByCode(code);
    }

}
