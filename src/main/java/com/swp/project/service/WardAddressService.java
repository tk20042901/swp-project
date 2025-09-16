package com.swp.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.project.entity.WardAddress;
import com.swp.project.repository.WardAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class WardAddressService {

    private final WardAddressRepository wardAddressRepository;

    public List<WardAddress> getAllWard() {
        return wardAddressRepository.findAll();
    }

    public void initWard() {
        try {
            List<WardAddress> wards = new ObjectMapper().readValue(
                    WardAddressService.class
                            .getClassLoader()
                            .getResourceAsStream("ward.json"),
                    new TypeReference<>() {}
            );
            for (WardAddress wardAddress : wards) {
                if (!wardAddressRepository.existsById(wardAddress.getCode())) {
                    wardAddressRepository.save(wardAddress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
